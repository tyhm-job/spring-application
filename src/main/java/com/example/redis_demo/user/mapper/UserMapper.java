package com.example.redis_demo.user.mapper;

import com.example.redis_demo.user.dto.request.RegisterRequest;
import com.example.redis_demo.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Giúp Spring quản lý như một Bean
public interface UserMapper {

    // Mapping từ Request DTO sang Entity
    @Mapping(target = "id", ignore = true) // id để DB tự sinh
    @Mapping(target = "passwordHash", ignore = true) // mật khẩu sẽ xử lý riêng
    User toEntity(RegisterRequest request);
}