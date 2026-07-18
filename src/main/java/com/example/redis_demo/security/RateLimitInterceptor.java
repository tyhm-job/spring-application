package com.example.redis_demo.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest; // Import chuẩn cho Spring Boot 4.x
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        String key = "rate_limit:" + ip;

        // Tăng giá trị đếm trong Redis
        Long count = redisTemplate.opsForValue().increment(key);

        // Nếu mới tạo key, đặt thời gian sống (TTL) là 1 phút
        if (count != null && count == 1) {
            redisTemplate.expire(key, java.time.Duration.ofMinutes(1));
        }

        // Nếu quá 60 request trong 1 phút -> chặn
        if (count != null && count > 60) {
            response.setStatus(429);
            response.getWriter().write("Too many requests!");
            return false;
        }

        return true;
    }
}