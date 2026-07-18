package com.example.redis_demo.repository;

import com.example.redis_demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Không cần viết code, JPA đã tự làm hết các lệnh CRUD (save, findById, delete...)
}