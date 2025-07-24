package com.seckill.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "seckill_order",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "product_id"})}
)
public class SeckillOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity = 1;

    /**
     * 订单状态：0-待支付，1-已支付，2-取消
     */
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus = 0;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public SeckillOrder() {
        // 默认构造函数
    }

    // 带参构造
    public SeckillOrder(Long userId, Long productId, Integer quantity, Integer orderStatus,
                        LocalDateTime createTime, LocalDateTime updateTime) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}