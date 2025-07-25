package com.seckill.service;

import com.seckill.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
    @Autowired
    private RedisService redisService;

    public List<Product> getProductList() {
        return List.of(
                new Product(1L, "iPhone 15", 500, LocalDateTime.now(), LocalDateTime.now()),
                new Product(2L, "MacBook Pro", 30, LocalDateTime.now(), LocalDateTime.now()),
                new Product(3L, "iPad Air", 20, LocalDateTime.now(), LocalDateTime.now())
        );
    }

    // 启动预热
    public void preloadStockToRedis() {
        log.info("开始预热库存到 Redis...");
        for (Product product : getProductList()) {
            try {
                redisService.setStock(product.getId(), product.getStock());
                log.info("预热成功：productId={}, stock={}", product.getId(), product.getStock());
            } catch (Exception e) {
                log.error("预热失败：productId={}, error={}", product.getId(), e.getMessage());
            }
        }
        log.info("商品库存预热完成。");
    }
}
