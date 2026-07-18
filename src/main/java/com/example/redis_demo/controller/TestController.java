package com.example.redis_demo.controller;

import com.example.redis_demo.model.Customer;
import com.example.redis_demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class TestController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/saveCustomer")
    public String saveCustomer(@RequestBody Customer customer) {
        customerService.saveCustomer(customer);
        return "Đã lưu customer: " + customer.getName();
    }

    @GetMapping("/getCustomerById/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        return customerService.getCustomerById(id);
    }
}
