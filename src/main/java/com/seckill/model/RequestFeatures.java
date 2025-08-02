package com.seckill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestFeatures {
    private String ipAddress;
    private String userAgent;
    private Integer requestPerMinute;
    private String sessionId;
    private Long productId;
    private Long timestamp;
}
