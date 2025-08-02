package com.seckill.service;

import com.seckill.model.RequestFeatures;
import com.seckill.util.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;  // Spring自带HTTP客户端
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RiskControlService {

    private final Map<String, List<Long>> ipRequestLog = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    // Python风控服务地址
    private static final String RISK_CONTROL_URL = "http://localhost:5000/score";

    public RequestFeatures extractFeatures(HttpServletRequest request, Long productId, Long userId) {
        String ip = IPUtils.getClientIpAddr(request);
        String ua = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();
        long timestamp = System.currentTimeMillis();

        int freq = updateAndCount(ip, timestamp);

        return new RequestFeatures(ip, ua, freq, sessionId, userId, productId, timestamp);
    }

    private int updateAndCount(String ip, long currentTimeMillis) {
        ipRequestLog.putIfAbsent(ip, new ArrayList<>());
        List<Long> timestamps = ipRequestLog.get(ip);
        timestamps.add(currentTimeMillis);
        timestamps.removeIf(t -> t < currentTimeMillis - 60_000);
        return timestamps.size();
    }

    /**
     * 调用Python风控服务判断是否放行
     * @param features 请求特征
     * @return true 表示允许，false表示拦截
     */
    public boolean checkRisk(RequestFeatures features) {
        try {
            String json = String.format(
                    "{ \"ip\": \"%s\", " +
                            "\"userAgent\": \"%s\", " +
                            "\"userId\": \"%s\", " +
                            "\"productId\": %d, " +
                            "\"timestamp\": %d, " +
                            "\"requestCountLastMinute\": %d }",
                    features.getIpAddress(),
                    features.getUserAgent(),
                    features.getSessionId(),  // 这里用 sessionId 代替 userId，如果有真实 userId，请替换
                    features.getProductId(),
                    features.getTimestamp(),
                    features.getRequestPerMinute()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // 发送请求到风控服务
            ResponseEntity<String> response = restTemplate.postForEntity(RISK_CONTROL_URL, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().contains("\"decision\":\"ALLOW\"");
            }
        } catch (Exception e) {
            // 异常处理：记录日志并默认放行
            log.warn("风控服务调用失败，默认放行。异常信息：{}", e.getMessage());
        }
        return true; // 出现异常时默认返回放行（true）
    }
}
