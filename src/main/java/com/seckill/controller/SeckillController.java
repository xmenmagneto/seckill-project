package com.seckill.controller;

import com.seckill.kafka.KafkaSender;
import com.seckill.model.OrderMessage;
import com.seckill.service.RedisService;
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

    @Autowired
    private RedisService redisService;

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("接收到秒杀请求：userId={}, productId={}", userId, productId);

        // 1. 执行 Redis Lua 脚本（原子操作）
        Long result = redisService.trySeckill(productId, userId);
        log.info("Redis trySeckill 返回结果：{}", result);

        switch (result.intValue()) {
            case -1 -> throw new RuntimeException("商品库存未初始化");
            case 0 -> throw new RuntimeException("商品已售罄");
            case 2 -> throw new RuntimeException("重复下单，您已抢购过该商品");
            case 1 -> {
                // 2. 投递 Kafka 异步下单
                OrderMessage message = new OrderMessage(userId, productId);
                kafkaSender.sendOrderMessage("seckill-order", message.toJson());
                log.info("秒杀请求投递 Kafka 成功：{}", message);
                return "秒杀请求已提交，请稍后查看结果";
            }
            default -> throw new RuntimeException("未知错误，请稍后重试");
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
