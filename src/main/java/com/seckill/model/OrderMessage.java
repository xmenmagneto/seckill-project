package com.seckill.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderMessage {

    private Long userId;
    private Long productId;

    public OrderMessage() {
    }

    public OrderMessage(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    // 将对象转为 JSON 字符串
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    // 将 JSON 字符串转回对象（可选）
    public static OrderMessage fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, OrderMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}