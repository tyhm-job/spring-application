package com.example.redis_demo.user.services;

import com.example.redis_demo.exception.EmailAlreadyExistsException;
import com.example.redis_demo.user.dto.RegisterRequest;
import com.example.redis_demo.user.mapper.UserMapper;
import com.example.redis_demo.user.model.User;
import com.example.redis_demo.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email" + request.getEmail() + "đã tồn tại!");
        }

        // Tạo Entity và lưu vào DB
        User user = userMapper.toEntity(request);

        // Băm password từ password thô trước
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");

        userRepository.save(user);
    }

    public User getCustomerById(String id) {
        String key = "customer:" + id;
        User user = null;
        // 1. Tìm trong Redis
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj != null) {
            // Kiểm tra xem có phải là "giá trị đánh dấu" không
            if ("NULL".equals(obj)) {
                return null; // Trả về null ngay lập tức, không xuống DB nữa
            }

            return objectMapper.convertValue(obj, User.class);
        }

        // 2. Nếu không có trong Redis, tìm trong MySQL
        user = userRepository.findById(id).orElse(null);

        // 3. Xử lý logic chống Cache Penetration
        if (user == null) {
            // Nếu không có trong DB, lưu "NULL" vào Redis trong 5 phút
            redisTemplate.opsForValue().set(key, "NULL", Duration.ofMinutes(5));
        } else {
            // Nếu có trong DB, lưu object vào Redis
            redisTemplate.opsForValue().set(key, user, Duration.ofHours(1));
        }
        return user;
    }
}
