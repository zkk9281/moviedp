package com.zkk.moviedp.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMQTopicConfig {
    // 订单超时队列（TTL + 死信队列）
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
    public static final String ORDER_TIMEOUT_KEY = "order.timeout.key";

    // 订单超时死信队列（处理超时订单）
    public static final String ORDER_DEAD_LETTER_QUEUE = "order.deadletter.queue";
    public static final String ORDER_DEAD_LETTER_EXCHANGE = "order.deadletter.exchange";
    public static final String ORDER_DEAD_LETTER_KEY = "order.deadletter.key";

    // 普通订单队列
    public static final String QUEUE = "seckillQueue";
    public static final String EXCHANGE = "seckillExchange";
    public static final String ROUTINGKEY = "seckill.#";

    // 支付结果队列（处理支付回调）
    public static final String ORDER_PAYMENT_QUEUE = "order.payment.queue";
    public static final String ORDER_PAYMENT_EXCHANGE = "order.payment.exchange";
    public static final String ORDER_PAYMENT_KEY = "order.payment.key";

    // 订单TTL (30分钟)
    public static final int ORDER_TTL = 30 * 60 * 1000;
//    public static final int ORDER_TTL = 1000;

    // 普通订单队列
    @Bean
    public Queue queue(){
        return new Queue(QUEUE,true);
    }
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with(ROUTINGKEY);
    }

    // 配置订单超时队列（带TTL和死信交换机）
    @Bean
    Queue orderTimeoutQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDER_DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", ORDER_DEAD_LETTER_KEY);
        args.put("x-message-ttl", ORDER_TTL);
        return new Queue(ORDER_TIMEOUT_QUEUE, true, false, false, args);
    }

    @Bean
    DirectExchange orderTimeoutExchange() {
        return new DirectExchange(ORDER_TIMEOUT_EXCHANGE);
    }

    @Bean
    Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(ORDER_TIMEOUT_KEY);
    }

    // 配置订单超时死信队列
    @Bean
    Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DEAD_LETTER_QUEUE, true);
    }

    @Bean
    DirectExchange orderDeadLetterExchange() {
        return new DirectExchange(ORDER_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Binding orderDeadLetterBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderDeadLetterExchange())
                .with(ORDER_DEAD_LETTER_KEY);
    }

    // 配置支付结果队列
    @Bean
    Queue orderPaymentQueue() {
        return new Queue(ORDER_PAYMENT_QUEUE, true);
    }

    @Bean
    DirectExchange orderPaymentExchange() {
        return new DirectExchange(ORDER_PAYMENT_EXCHANGE);
    }

    @Bean
    Binding orderPaymentBinding() {
        return BindingBuilder.bind(orderPaymentQueue())
                .to(orderPaymentExchange())
                .with(ORDER_PAYMENT_KEY);
    }
//    private static final String QUEUE01="queue_topic01";
//    private static final String QUEUE02="queue_topic02";
//    private static final String EXCHANGE = "topicExchange";
//    private static final String ROUTINGKEY01 = "#.queue.#";
//    private static final String ROUTINGKEY02 = "*.queue.#";
//    @Bean
//    public Queue topicqueue01(){
//        return new Queue(QUEUE01);
//    }
//    @Bean
//    public Queue topicqueue02(){
//        return new Queue(QUEUE02);
//    }
//    @Bean
//    public TopicExchange topicExchange(){
//        return new TopicExchange(EXCHANGE);
//    }
//    @Bean
//    public Binding topicbinding01(){
//        return BindingBuilder.bind(topicqueue01()).to(topicExchange()).with(ROUTINGKEY01);
//    }
//    @Bean
//    public Binding topicbinding02(){
//        return BindingBuilder.bind(topicqueue02()).to(topicExchange()).with(ROUTINGKEY02);
//    }
}
