package com.example.redis_demo.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.redis_demo.model.Customer;
import com.example.redis_demo.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper; // Dùng import của Jackson 3.x

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testGetCustomerById_FoundInRedis() {
        Customer expected = new Customer("1", "John");
        String mockJson = "{\"id\":\"1\", \"name\":\"John\"}";

        // Gán Mock cho opsForValue
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("customer:1")).thenReturn(mockJson);
        when(objectMapper.convertValue(eq(mockJson), eq(Customer.class))).thenReturn(expected);

        Customer result = customerService.getCustomerById("1");

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(customerRepository, times(0)).findById(any()); // Đảm bảo không gọi DB
    }

    @Test
    void testGetCustomerById_NotFoundAnywhere() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("customer:99")).thenReturn(null);
        when(customerRepository.findById("99")).thenReturn(Optional.empty());

        Customer result = customerService.getCustomerById("99");

        assertNull(result);
        // Kiểm tra xem có lưu "NULL" vào Redis không (chống penetration)
        verify(valueOperations, times(1)).set(eq("customer:99"), eq("NULL"), any(Duration.class));
    }
}