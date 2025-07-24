package com.seckill.controller;

import com.seckill.kafka.KafkaSender;
import com.seckill.model.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaSender kafkaSender;

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("接收到秒杀请求：userId={}, productId={}", userId, productId);

        // 1. 判断是否重复抢购
        String key = "seckill:user:" + userId + ":product:" + productId;
        Boolean hasOrdered;
        try {
            hasOrdered = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(hasOrdered)) {
                log.warn("重复下单：userId={}, productId={}", userId, productId);
                return "You have already placed an order for this product";
            }
        } catch (Exception e) {
            log.error("Redis操作失败，userId={}, productId={}, 错误信息：{}", userId, productId, e.getMessage(), e);
            // 这里可以决定是否允许继续下单请求，或者直接返回失败提示
            return "系统异常，请稍后重试";
        }

        try {
            // 2. 发送下单消息到 Kafka
            OrderMessage message = new OrderMessage(userId, productId);
            kafkaSender.sendOrderMessage("seckill-order", message.toJson());
            log.info("秒杀请求已投递 Kafka：userId={}, productId={}", userId, productId);
            return "Purchase request submitted";
        } catch (Exception e) {
            log.error("Kafka发送消息失败，userId={}, productId={}, 错误信息：{}", userId, productId, e.getMessage(), e);
            // 这里可以做一些补偿逻辑，比如删除redis的防重复key，让用户可以重试，或者返回友好提示
            redisTemplate.delete(key);
            return "系统繁忙，请稍后重试";
        }
    }

    @GetMapping("/result")
    public String getSeckillResult(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("查询秒杀结果：userId={}, productId={}", userId, productId);

        String key = "seckill:result:" + userId + ":" + productId;
        try {
            String result = redisTemplate.opsForValue().get(key);
            return result != null ? result : "PENDING";
        } catch (Exception e) {
            log.error("查询秒杀结果失败，key={}", key, e);
            // 这里可以根据业务需求决定返回什么，暂时返回失败状态或PENDING
            return "ERROR";
        }
    }
}
