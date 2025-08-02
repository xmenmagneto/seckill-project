package com.seckill.util;

import jakarta.servlet.http.HttpServletRequest;

public class IPUtils {
    public static String getClientIpAddr(HttpServletRequest request) {
        String[] headerKeys = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };
        for (String key : headerKeys) {
            String ip = request.getHeader(key);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0]; // 多个IP时取第一个
            }
        }
        return request.getRemoteAddr();
    }
}
