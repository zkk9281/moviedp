package com.zkk.moviedp.rebbitmq;

import com.alibaba.fastjson.JSON;
import com.zkk.moviedp.config.RabbitMQTopicConfig;
import com.zkk.moviedp.entity.VoucherOrder;
import com.zkk.moviedp.service.ISeckillVoucherService;
import com.zkk.moviedp.service.IVoucherOrderService;
import com.zkk.moviedp.constants.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * 消息消费者
 */
@Slf4j
@Service
public class MQReceiver {

    @Autowired
    IVoucherOrderService voucherOrderService;
    @Autowired
    ISeckillVoucherService seckillVoucherService;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> TIMEOUT_SCRIPT;
    static {
        TIMEOUT_SCRIPT = new DefaultRedisScript<>();
        TIMEOUT_SCRIPT.setLocation(new ClassPathResource("timeout.lua"));
        TIMEOUT_SCRIPT.setResultType(Long.class);
    }
    /**
     * 接收秒杀信息并下单
     * @param msg
     */
    @Transactional
    @RabbitListener(queues = RabbitMQTopicConfig.QUEUE)
    public void receiveSeckillMessage(String msg){
        log.info("接收到消息: "+msg);
        VoucherOrder voucherOrder = JSON.parseObject(msg, VoucherOrder.class);

        Long orderId = voucherOrder.getId();

        Long voucherId = voucherOrder.getVoucherId();
        //5.一人一单
        Long userId = voucherOrder.getUserId();
        //5.1查询订单
        long count = voucherOrderService.query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        //5.2判断是否存在
        if(count>0){
            //用户已经购买过了
            log.error("该用户已购买过");
            return ;
        }
        log.info("扣减库存");
        //6.扣减库存
        boolean success = seckillVoucherService
                .update()
                .setSql("stock = stock-1")
                .eq("voucher_id", voucherId)
                .gt("stock",0)//cas乐观锁
                .update();
        if(!success){
            log.error("库存不足");
            return;
        }
        //直接保存订单
        voucherOrderService.save(voucherOrder);
        mqSender.sendPayMessage(msg);

    }

    @Transactional
    @RabbitListener(queues = RabbitMQTopicConfig.ORDER_DEAD_LETTER_QUEUE)
    public void handleTimeoutOrder(String msg) {
        try {
            log.info("死信队列接收到消息: "+msg);
            VoucherOrder voucherOrder = JSON.parseObject(msg, VoucherOrder.class);
            Long orderId = voucherOrder.getId();
            Long voucherId = voucherOrder.getVoucherId();
            Long userId = voucherOrder.getUserId();
            // 1. 查询订单状态
            VoucherOrder order = voucherOrderService.getById(orderId);
            // 2. 仅处理未支付的订单
            if (order.getStatus() == OrderStatus.UNPAID) {
                // 3. 执行订单取消逻辑（释放库存、标记已取消等）
                order.setStatus(OrderStatus.CANCELLED);
                voucherOrderService.updateById(order);
                // 恢复库存
                seckillVoucherService.update()
                        .setSql("stock = stock+1")
                        .eq("voucher_id", voucherId)
                        .update();
                // 执行脚本处理redis库存和订单
                stringRedisTemplate.execute(
                        TIMEOUT_SCRIPT,
                        Collections.emptyList(),
                        voucherId.toString(),
                        userId.toString()
                );
                log.info("订单超时取消成功: " + orderId);
            }
        } catch (Exception e) {
            // 处理异常（记录日志、重试等）
            log.error("处理超时订单失败: {}", msg, e);
            throw new RuntimeException("处理超时订单失败", e);
        }
    }
}
