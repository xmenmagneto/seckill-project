package com.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setStock(Long productId, Integer stock) {
        redisTemplate.opsForValue().set("seckill:stock:" + productId, stock.toString());
    }

    public Integer getStock(Long productId) {
        String value = redisTemplate.opsForValue().get("seckill:stock:" + productId);
        return value == null ? 0 : Integer.parseInt(value);
    }
}