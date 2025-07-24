package com.seckill.filter;

import com.seckill.common.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String uri = request.getRequestURI();

            // 只对 /seckill 开头的接口限流
            if (uri.startsWith("/seckill")) {
                String ip = request.getRemoteAddr();
                boolean allowed = rateLimiterService.isAllowed(ip);

                if (!allowed) {
                    log.warn("请求被限流：IP={}, URI={}", ip, uri);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":429, \"message\":\"请求过于频繁，请稍后再试\"}");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("限流过滤器异常：{}", e.getMessage(), e);
            throw e; // 保证异常继续抛出，由 SpringBoot GlobalExceptionHandler 兜底
        }
    }
}