package com.example.redis_demo.user.services;

import com.example.redis_demo.common.exception.AppException;
import com.example.redis_demo.common.enums.ResponseCode;
import com.example.redis_demo.security.JwtProvider;
import com.example.redis_demo.user.dto.request.LoginRequest;
import com.example.redis_demo.user.dto.request.RegisterRequest;
import com.example.redis_demo.user.dto.request.VerifyRequest;
import com.example.redis_demo.user.mapper.UserMapper;
import com.example.redis_demo.user.model.User;
import com.example.redis_demo.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProvider jwtProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ResponseCode.EMAIL_ALREADY_EXISTS);
        }

        // Create Entity and save to DB
        User user = userMapper.toEntity(request);

        // Generate PasswordHash from original Password by BCryptPasswordEncoder
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("PENDING");

        userRepository.save(user);

        // Generate OTP
        String otp = String.valueOf(new Random().nextInt(100000, 999999));

        try {
            // 3. Save to Redis (TTL = 10 mins)
            redisTemplate.opsForValue().set("otp:" + request.getEmail(), otp, Duration.ofMinutes(10));
        } catch (Exception e) {
            throw new AppException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void verify(VerifyRequest request) {
        String cachedOtpKey = "otp:" + request.getEmail();
        String cachedOtp = redisTemplate.opsForValue().get(cachedOtpKey);

        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            throw new AppException(ResponseCode.INVALID_OTP);
        }

        if (cachedOtp != null && cachedOtp.equals(request.getOtp())) {
            // Xác thực thành công: update status trong MySQL
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));
            user.setStatus("ACTIVE");
            userRepository.save(user);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // Xóa OTP trong Redis sau khi transaction đã commit
                    redisTemplate.delete(cachedOtpKey);
                }
            });
        } else {
            throw new AppException(ResponseCode.OTP_EXPIRED);
        }
    }

    @Transactional
    public String login(LoginRequest request) {
        String cachedNumOfLogKey = "numOfLog:" + request.getEmail();
        String cachedNumOfLogValue = redisTemplate.opsForValue().get(cachedNumOfLogKey);
        int cachedNumOfLog = cachedNumOfLogValue != null ?
                Integer.parseInt(cachedNumOfLogValue) : 0;

        if (cachedNumOfLog >= 5) {
            throw new AppException(ResponseCode.MAXIMUM_NUMBER_OF_ATTEMPTS);
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            redisTemplate.delete(cachedNumOfLogKey);

            return jwtProvider.generateToken(user);
        } else {
            redisTemplate.opsForValue().set(cachedNumOfLogKey, String.valueOf(++cachedNumOfLog),
                    Duration.ofMinutes(15));

            throw new AppException(ResponseCode.INVALID_CREDENTIALS);
        }
    }
}
