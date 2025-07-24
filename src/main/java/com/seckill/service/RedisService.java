package com.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setStock(Long productId, Integer stock) {
        String key = "seckill:stock:" + productId;
        redisTemplate.opsForValue().set(key  + productId, stock.toString());
        log.info("Redis写入库存：key={}, value={}", key, stock);
    }

    public Integer getStock(Long productId) {
        String key = "seckill:stock:" + productId;
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.warn("Redis库存为空：key={}", key);
                return 0;
            }
            log.info("Redis读取库存：key={}, value={}", key, value);
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Redis读取库存失败：key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("读取 Redis 库存失败");
        }
    }
}