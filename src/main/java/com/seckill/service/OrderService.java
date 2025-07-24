package com.seckill.service;

import com.seckill.model.Product;
import com.seckill.model.SeckillOrder;
import com.seckill.repository.ProductRepository;
import com.seckill.repository.SeckillOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private SeckillOrderRepository orderRepository;

    @Autowired
    ProductRepository  productRepository;

    /**
     * 创建订单（包含库存扣减和订单写库）
     * 事务控制保证一致性
     */@Transactional
    public void createOrder(Long userId, Long productId) {
        // 1. 查询商品库存
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() <= 0) {
            throw new RuntimeException("库存不足");
        }

        // 2. 判断是否已抢购（幂等控制）
        if (orderRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("您已抢购过该商品");
        }

        // 3. 扣减库存
        product.setStock(product.getStock() - 1);
        productRepository.save(product);

        // 4. 新建订单
        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setOrderStatus(0); // 0-待支付
        orderRepository.save(order);
    }
}