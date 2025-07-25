package com.seckill.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

@Slf4j
@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int LIMIT = 2000; // 每秒最大请求数

    /**
     * 根据 IP 地址判断是否允许请求（每秒最多 N 次）
     */
    public boolean isAllowed(String ip) {
        try {
            String second = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            String key = "seckill:limit:" + ip + ":" + second;

            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(1));
            }

            if (count > LIMIT) {
                log.warn("访问频率过高，限流触发：ip={}, count={}", ip, count);
                return false;
            }

            log.debug("限流通过：ip={}, count={}", ip, count);
            return true;
        } catch (Exception e) {
            log.error("限流时 Redis 操作失败，自动放行：ip={}", ip, e);
            return true; // Redis 异常时放行，避免服务不可用
        }
    }
}