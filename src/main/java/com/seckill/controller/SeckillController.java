package com.seckill.controller;

import com.seckill.kafka.KafkaSender;
import com.seckill.model.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaSender kafkaSender;

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId, @RequestParam Long productId) {
        // 1. 判断是否重复抢购
        String key = "seckill:user:" + userId + ":product:" + productId;
        Boolean hasOrdered = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(hasOrdered)) {
            return "You have already placed an order for this product";
        }

        // 2. 发送下单消息到 Kafka
        OrderMessage message = new OrderMessage(userId, productId);
        kafkaSender.sendOrderMessage("seckill-order", message.toJson());

        return "Purchase request submitted";
    }
}
