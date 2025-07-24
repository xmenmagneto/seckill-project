package com.seckill.repository;

import com.seckill.model.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}