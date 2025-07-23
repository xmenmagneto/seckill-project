package com.seckill.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @KafkaListener(topics = "seckill-order", groupId = "seckill-group")
    public void consume(String message) {
        System.out.println("消费到订单消息: " + message);
        // 后续会解析 JSON 并处理订单逻辑
    }
}
