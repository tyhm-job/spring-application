package com.example.redis_demo.user.services;

import com.example.redis_demo.common.enums.ResponseCode;
import com.example.redis_demo.common.exception.AppException;
import com.example.redis_demo.security.JwtProvider;
import com.example.redis_demo.user.dto.request.LoginRequest;
import com.example.redis_demo.user.dto.request.RegisterRequest;
import com.example.redis_demo.user.dto.request.VerifyRequest;
import com.example.redis_demo.user.mapper.UserMapper;
import com.example.redis_demo.user.model.User;
import com.example.redis_demo.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserMapper userMapper;

    // Dùng @Spy cho passwordEncoder để có thể mã hóa thực tế mà không bị lỗi UnnecessaryStubbingException
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void login_success() {
        // SET-UP
        LoginRequest request = new LoginRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setEmail("tyhm@gmail.com");
        mockUser.setPasswordHash("hashPassword");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("0");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(mockUser)).thenReturn("anyString()");

        // WHEN
        String token = userService.login(request);

        // THEN
        assertNotNull(token);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void login_fail_wrong_password() {
        // SET-UP
        LoginRequest request = new LoginRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setEmail("tyhm@gmail.com");
        mockUser.setPasswordHash("hashPassword");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("0");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> {
            userService.login(request);
        });

        // THEN
        assertEquals(ResponseCode.INVALID_CREDENTIALS, exception.getResponseCode());
        verify(valueOperations, times(1)).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void login_fail_email_does_not_exist() {
        // SET-UP
        LoginRequest request = new LoginRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("123456");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("3");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> {
            userService.login(request);
        });

        // THEN
        assertEquals(ResponseCode.EMAIL_DOES_NOT_EXIST, exception.getResponseCode());
    }

    @Test
    void login_fail_maximum_attemps() {
        // SET-UP
        LoginRequest request = new LoginRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("123456");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("6");

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> {
            userService.login(request);
        });

        // THEN
        assertEquals(ResponseCode.MAXIMUM_NUMBER_OF_ATTEMPTS, exception.getResponseCode());
    }

    @Test
    void verify_success() {
        VerifyRequest request = new VerifyRequest();
        request.setEmail("tyhm@gmail.com");
        request.setOtp("123456");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        when(redisTemplate.delete(anyString())).thenReturn(true);

        // WHEN
        assertDoesNotThrow(() -> userService.verify(request));

        // THEN
        verify(valueOperations, times(1)).get(anyString());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void verify_fail_invalid_otp() {
        // SET-UP
        VerifyRequest request = new VerifyRequest();
        request.setEmail("tyhm@gmail.com");
        request.setOtp("123456");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> {
            userService.verify(request);
        });

        // THEN
        assertEquals(ResponseCode.OTP_INVALID_OR_EXPIRED, exception.getResponseCode());

        // Đảm bảo không gọi xuống save vì OTP đã sai
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void verify_fail_emailDoesNotExist() {
        // SET-UP
        VerifyRequest request = new VerifyRequest();
        request.setEmail("tyhm@gmail.com");
        request.setOtp("123456");

        // GIVEN
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> {
            userService.verify(request);
        });

        // THEN
        assertEquals(ResponseCode.EMAIL_DOES_NOT_EXIST, exception.getResponseCode());

        // Đảm bảo không gọi xuống save vì email không tồn tại
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void register_success() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("123456");
        request.setDisplayName("Ho Minh Ty");
        request.setCurrency("VND");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Giả lập Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> userService.register(request));

        // Kiểm tra xem các hàm quan trọng có thực sự được gọi hay không
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(valueOperations, times(1)).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void register_fail_emailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("123456");
        request.setDisplayName("Ho Minh Ty");
        request.setCurrency("VND");

        // GIVEN: Giả lập email đã tồn tại trong DB
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // WHEN & THEN: Kỳ vọng quăng ra AppException với mã lỗi EMAIL_ALREADY_EXISTS
        AppException exception = assertThrows(AppException.class, () -> {
            userService.register(request);
        });

        assertEquals(ResponseCode.EMAIL_ALREADY_EXISTS, exception.getResponseCode());

        // Đảm bảo không gọi xuống save hay redis nếu email đã tồn tại
        verify(userRepository, never()).save(any(User.class));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void register_fail_redisError() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tyhm@gmail.com");
        request.setPassword("123456");
        request.setDisplayName("Ho Minh Ty");
        request.setCurrency("VND");

        // GIVEN
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Giả lập Redis bị lỗi khi gọi lệnh set (quăng ra Exception)
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        // WHEN & THEN: Kỳ vọng bắt được AppException do khối catch xử lý
        AppException exception = assertThrows(AppException.class, () -> {
            userService.register(request);
        });

        assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, exception.getResponseCode());
    }
}