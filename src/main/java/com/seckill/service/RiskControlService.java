package com.seckill.service;

import com.seckill.model.RequestFeatures;
import com.seckill.util.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskControlService {

    // 简单内存统计模拟频率（可替换为Redis计数）
    private final Map<String, List<Long>> ipRequestLog = new ConcurrentHashMap<>();

    public RequestFeatures extractFeatures(HttpServletRequest request, Long productId) {
        String ip = IPUtils.getClientIpAddr(request);
        String ua = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();
        long timestamp = System.currentTimeMillis();

        // 统计一分钟内请求频率
        int freq = updateAndCount(ip, timestamp);

        return new RequestFeatures(ip, ua, freq, sessionId, productId, timestamp);
    }

    private int updateAndCount(String ip, long currentTimeMillis) {
        ipRequestLog.putIfAbsent(ip, new ArrayList<>());
        List<Long> timestamps = ipRequestLog.get(ip);
        timestamps.add(currentTimeMillis);
        // 清除过期请求
        timestamps.removeIf(t -> t < currentTimeMillis - 60_000);
        return timestamps.size();
    }
}
