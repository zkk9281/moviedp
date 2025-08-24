package com.zkk.moviedp.service.impl;

import com.alibaba.fastjson.JSON;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.VoucherOrder;
import com.zkk.moviedp.mapper.VoucherOrderMapper;
import com.zkk.moviedp.rebbitmq.MQSender;
import com.zkk.moviedp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.constants.OrderStatus;
import com.zkk.moviedp.utils.RedisIDWorker;
import com.zkk.moviedp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

//    @Resource
//    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

//    @Resource
//    private RedissonClient redissonClient;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisIDWorker redisIDWorker;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

//    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
//    private IVoucherOrderService proxy;

    // 消息队列,下单
    @Override
    public Result secKillVoucher(Long voucherId) {
        //1.执行lua脚本
        Long userId = UserHolder.getUser().getId();

        Long r = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        //2.判断结果为0
        int result = r.intValue();
        if (result != 0) {
            //2.1不为0代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "该用户重复下单");
        }
        //2.2为0代表有购买资格,将下单信息保存到阻塞队列

        //2.3创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //2.4订单id
        long orderId = redisIDWorker.nextId("order");
        voucherOrder.setId(orderId);
        //2.5用户id
        voucherOrder.setUserId(userId);
        //2.6代金卷id
        voucherOrder.setVoucherId(voucherId);
        // 代金券状态
        voucherOrder.setStatus(OrderStatus.UNPAID);
        //2.7将信息放入MQ中
        mqSender.sendSeckillMessage(JSON.toJSONString(voucherOrder));

        //2.7 返回订单id
        return Result.ok(orderId);
    }
//    public Result secKillVoucher(Long voucherId) {
//        Long userId = UserHolder.getUser().getId();
//        long orderId = redisIDWorker.nextId("order");
//        //执行lua脚本
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Collections.emptyList(),
//                voucherId.toString(), userId.toString(), String.valueOf(orderId)
//        );
//        //非0，没有资格
//        int r = result.intValue();
//        if(r != 0) {
//            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
//        }
//
//        proxy = (IVoucherOrderService) AopContext.currentProxy();
//
//        return Result.ok(orderId);
//    }

    // 类加载时运行
//    @PostConstruct
//    private void init() {
//        // 创建一个新线程
//        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
//    }

//    private class VoucherOrderHandler implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 获取redis消息队列中的订单消息
//                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
//                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
//                    );
//                    if(list == null || list.isEmpty()) {
//                        continue;
//                    }
//                    // 解析消息
//                    MapRecord<String, Object, Object> record = list.get(0);
//                    Map<Object, Object> values = record.getValue();
//                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
//                    // 处理订单消息
//                    handleVoucherOrder(voucherOrder);
//                    // 消息确认
//                    stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", record.getId());
//                } catch (Exception e) {
//                    log.error("处理订单异常", e);
//                    handlePendingList();
//                }
//            }
//        }

    /*// 阻塞队列
    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    // 异步多线程处理订单
    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    VoucherOrder voucherOrder = orderTasks.take();
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }*/

//        private void handleVoucherOrder(VoucherOrder voucherOrder) {
//            proxy.createVoucherOrder(voucherOrder);
//        }
//
//        private void handlePendingList() {
//            while (true) {
//                try {
//                    // 获取redis消息队列中的订单消息
//                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1),
//                            StreamOffset.create("stream.orders", ReadOffset.from("0"))
//                    );
//                    if(list == null || list.isEmpty()) {
//                        break;
//                    }
//                    // 解析消息
//                    MapRecord<String, Object, Object> record = list.get(0);
//                    Map<Object, Object> values = record.getValue();
//                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
//                    // 处理消息
//                    handleVoucherOrder(voucherOrder);
//                    // 消息确认
//                    stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", record.getId());
//                } catch (Exception e) {
//                    log.error("处理pending-list异常", e);
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException interruptedException) {
//                        interruptedException.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    /*// 用户下单秒杀券 redis优化秒杀资格判断
    @Override
    public Result secKillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        //执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        //非0，没有资格
        int r = result.intValue();
        if(r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        //为0，有资格保存下单信息到队列
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIDWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        orderTasks.add(voucherOrder);

        proxy = (IVoucherOrderService) AopContext.currentProxy();

        return Result.ok(orderId);
    }*/

    /* //未使用redis优化
    @Override
    public Result secKillVoucher(Long voucherId) {
        // 判断优惠券是否合法
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        if(voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀未开始");
        }

        if(voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已结束");
        }

        if (voucher.getStock() < 1) {
            return Result.fail("库存不足");
        }

        Long userId = UserHolder.getUser().getId();

//        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);

        boolean isLock = lock.tryLock();
        if(!isLock) {
            return Result.fail("不允许重复下单");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }*/

//    @Transactional
//    public void createVoucherOrder(VoucherOrder voucherOrder) {
//        // 扣库存
//        boolean success = seckillVoucherService.update()
//                .setSql("stock = stock - 1")
//                .eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0) // 解决并发超卖
//                .update();
//        if(!success) {
//            log.error("库存不足");
//            return;
//        }
//        save(voucherOrder);
//    }
}
