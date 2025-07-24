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

        try {
            OrderMessage orderMessage = OrderMessage.fromJson(message);
            orderService.createOrder(orderMessage.getUserId(), orderMessage.getProductId());
        } catch (Exception e) {
            log.error("处理秒杀订单失败，消息内容：{}，异常信息：{}", message, e.getMessage(), e);
            // 根据业务，选择：
            // 1. 忽略异常，继续消费下一条（当前catch即为忽略异常）
            // 2. 手动重试或将消息转发到死信队列
        }
    }
}