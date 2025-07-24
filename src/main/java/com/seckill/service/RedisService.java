package com.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    public void setStock(Long productId, Integer stock) {
        String key = "seckill:stock:" + productId;
        redisTemplate.opsForValue().set(key  + productId, stock.toString());
        log.info("Redis写入库存：key={}, value={}", key, stock);
    }

    public Integer getStock(Long productId) {
        String key = "seckill:stock:" + productId;
        String value = redisTemplate.opsForValue().get(key + productId);
        if (value == null) {
            log.warn("Redis库存为空：key={}", key);
            return 0;
        }
        log.info("Redis读取库存：key={}, value={}", key, value);
        return Integer.parseInt(value);
    }
}