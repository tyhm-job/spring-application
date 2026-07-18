package com.example.redis_demo.service;

import com.example.redis_demo.model.Customer;
import com.example.redis_demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void saveCustomer(Customer customer) {
        // Lưu DB
        customerRepository.save(customer);

        // Lưu Redis với thời gian hết hạn (TTL) để tránh đầy bộ nhớ
        // Ví dụ: Lưu 1 tiếng rồi tự xóa
        redisTemplate.opsForValue().set("customer:" + customer.getId(), customer, Duration.ofHours(1));
    }

    public Customer getCustomerById(String id) {
        String key = "customer:" + id;
        Customer customer = null;
        // 1. Tìm trong Redis
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj != null) {
            // Kiểm tra xem có phải là "giá trị đánh dấu" không
            if ("NULL".equals(obj)) {
                return null; // Trả về null ngay lập tức, không xuống DB nữa
            }

            return objectMapper.convertValue(obj, Customer.class);
        }

        // 2. Nếu không có trong Redis, tìm trong MySQL
        customer = customerRepository.findById(id).orElse(null);

        // 3. Xử lý logic chống Cache Penetration
        if (customer == null) {
            // Nếu không có trong DB, lưu "NULL" vào Redis trong 5 phút
            redisTemplate.opsForValue().set(key, "NULL", Duration.ofMinutes(5));
        } else {
            // Nếu có trong DB, lưu object vào Redis
            redisTemplate.opsForValue().set(key, customer, Duration.ofHours(1));
        }
        return customer;
    }
}
