package com.seckill.repository;

import com.seckill.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 如果需要，可以在这里加自定义查询方法
}