package com.seckill.controller;

import com.seckill.kafka.KafkaSender;
import com.seckill.model.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaSender kafkaSender;

    private static final Logger log = LoggerFactory.getLogger(SeckillController.class);

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("接收到秒杀请求：userId={}, productId={}", userId, productId);

        // 1. 判断是否重复抢购
        String key = "seckill:user:" + userId + ":product:" + productId;
        Boolean hasOrdered = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(hasOrdered)) {
            log.warn("重复下单：userId={}, productId={}", userId, productId);
            return "You have already placed an order for this product";
        }

        // 2. 发送下单消息到 Kafka
        OrderMessage message = new OrderMessage(userId, productId);
        kafkaSender.sendOrderMessage("seckill-order", message.toJson());
        log.info("秒杀请求已投递 Kafka：userId={}, productId={}", userId, productId);

        return "Purchase request submitted";
    }

    @GetMapping("/result")
    public String getSeckillResult(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("查询秒杀结果：userId={}, productId={}", userId, productId);

        String key = "seckill:result:" + userId + ":" + productId;
        String result = redisTemplate.opsForValue().get(key);
        return result != null ? result : "PENDING";
    }
}
