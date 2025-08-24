package com.zkk.moviedp.rebbitmq;

import com.zkk.moviedp.config.RabbitMQTopicConfig;
import com.zkk.moviedp.dto.PaymentResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者
 */
@Slf4j
@Service
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String ROUTINGKEY = "seckill.message";
    private static final long ORDER_TIMEOUT = 30 * 60 * 1000; // 30分钟
    /**
     * 发送秒杀信息
     * @param msg
     */
    public void sendSeckillMessage(String msg){
        log.info("发送消息"+msg);
        rabbitTemplate.convertAndSend(RabbitMQTopicConfig.EXCHANGE, ROUTINGKEY, msg);
    }

    public void sendPayMessage(String msg) {
        log.info("发送支付消息，订单id：{}", msg);
        rabbitTemplate.convertAndSend(
                RabbitMQTopicConfig.ORDER_TIMEOUT_EXCHANGE,
                RabbitMQTopicConfig.ORDER_TIMEOUT_KEY,
                msg
        );
    }

    // 发送支付结果消息
    public void sendPaymentResultMessage(PaymentResultMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQTopicConfig.ORDER_PAYMENT_EXCHANGE,
                RabbitMQTopicConfig.ORDER_PAYMENT_KEY,
                message
        );
    }
}
