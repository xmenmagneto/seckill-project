package com.seckill.kafka;

import com.seckill.model.OrderMessage;
import com.seckill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "seckill-order", groupId = "seckill-group")
    public void consume(String message) {
        System.out.println("消费到订单消息: " + message);

        // 反序列化 JSON -> OrderMessage
        OrderMessage orderMessage = OrderMessage.fromJson(message);

        // 调用 OrderService 创建订单
        orderService.createOrder(orderMessage.getUserId(), orderMessage.getProductId());
    }
}