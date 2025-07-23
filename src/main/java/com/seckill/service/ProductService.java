package com.seckill.service;

import com.seckill.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private RedisService redisService;

    public List<Product> getProductList() {
        return List.of(
                new Product(1L, "iPhone 15", 50),
                new Product(2L, "MacBook Pro", 30),
                new Product(3L, "iPad Air", 20)
        );
    }

    // 启动预热
    public void preloadStockToRedis() {
        for (Product product : getProductList()) {
            redisService.setStock(product.getId(), product.getStock());
        }
    }
}
