package com.seckill.config;

import com.seckill.service.ProductService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class StartupRunner {
    @Autowired
    private ProductService productService;

    @PostConstruct
    public void init() {
        productService.preloadStockToRedis();
        System.out.println("Redis preloading finished.");
    }
}