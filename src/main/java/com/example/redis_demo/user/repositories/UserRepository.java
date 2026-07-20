package com.example.redis_demo.user.repositories;

import com.example.redis_demo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Không cần viết code, JPA đã tự làm hết các lệnh CRUD (save, findById, delete...)
    boolean existsByEmail(String email);
}