package com.seckill.service;

import com.seckill.model.Product;
import com.seckill.model.SeckillOrder;
import com.seckill.repository.ProductRepository;
import com.seckill.repository.SeckillOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private SeckillOrderRepository orderRepository;

    @Autowired
    ProductRepository  productRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 创建订单（包含库存扣减和订单写库）
     * 事务控制保证一致性
     */@Transactional
    public void createOrder(Long userId, Long productId) {
        log.info("开始处理秒杀请求：userId={}, productId={}", userId, productId);

        // 1. 查询商品库存
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("商品不存在，productId={}", productId);
                    return new RuntimeException("商品不存在");
                });

        if (product.getStock() <= 0) {
            log.warn("库存不足，productId={}, 当前库存={}", productId, product.getStock());
            throw new RuntimeException("库存不足");
        }

        // 2. 判断是否已抢购（幂等控制）
        if (orderRepository.existsByUserIdAndProductId(userId, productId)) {
            log.warn("重复抢购，userId={}, productId={}", userId, productId);
            throw new RuntimeException("您已抢购过该商品");
        }

        // 3. 扣减库存
        int originalStock = product.getStock();
        product.setStock(product.getStock() - 1);
        productRepository.save(product);
        log.info("扣减库存成功，productId={}, 原库存={}, 新库存={}", productId, originalStock, product.getStock());

        // 4. 新建订单
        SeckillOrder order = new SeckillOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setOrderStatus(0); // 0-待支付
        orderRepository.save(order);
        log.info("创建订单成功，userId={}, productId={}, orderId={}", userId, productId, order.getId());


        // 5. 写入秒杀结果（前端轮询）
        redisTemplate.opsForValue().set("seckill:result:" + userId + ":" + productId, "SUCCESS");
        log.info("写入秒杀结果成功，key={}，value=SUCCESS", "seckill:result:" + userId + ":" + productId);
    }
}