package com.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setStock(Long productId, Integer stock) {
        String key = "seckill:stock:" + productId;
        redisTemplate.opsForValue().set(key, stock.toString());
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

    /**
     * 使用 Lua 脚本执行原子扣库存 + 幂等判断操作
     * @param productId 商品 ID
     * @param userId 用户 ID
     * @return 结果码：
     *         -1：库存未初始化
     *          0：库存不足
     *          1：扣库存成功
     *          2：用户已抢过（幂等）
     */
    public Long trySeckill(Long productId, Long userId) {
        // Lua 脚本内容
        String luaScript = """
            -- 获取商品库存
            local stock = tonumber(redis.call('get', KEYS[1]))
            if not stock then
                return -1  -- 库存未初始化
            end
            if stock <= 0 then
                return 0  -- 库存不足
            end

            -- 检查用户是否已抢购（幂等控制）
            if redis.call('exists', KEYS[2]) == 1 then
                return 2  -- 重复抢购
            end

            -- 扣减库存并标记用户
            redis.call('decr', KEYS[1])
            redis.call('set', KEYS[2], 1)
            return 1  -- 成功
        """;

        // 包装 Lua 脚本对象
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        // Redis key 设计
        String stockKey = "seckill:stock:" + productId;  // 商品库存
        String userKey = "seckill:user:" + userId + ":product:" + productId;  // 用户下单标记

        // 执行 Lua 脚本，传入 2 个 key
        return redisTemplate.execute(script, List.of(stockKey, userKey));
    }
}