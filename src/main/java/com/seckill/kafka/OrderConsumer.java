package com.seckill.kafka;

import com.seckill.model.OrderMessage;
import com.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderConsumer {

    @Autowired
    private OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "seckill-order", groupId = "seckill-group")
    public void consume(String message) {
        log.info("收到 Kafka 消息：{}", message);

        // 反序列化 JSON -> OrderMessage
        OrderMessage orderMessage = OrderMessage.fromJson(message);

        // 调用 OrderService 创建订单
        orderService.createOrder(orderMessage.getUserId(), orderMessage.getProductId());
    }
}