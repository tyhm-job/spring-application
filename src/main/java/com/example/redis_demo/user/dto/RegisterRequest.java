package com.example.redis_demo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequest {
    @NotBlank(message = "Email không được để trống")
    @Size(min = 3, message = "Task phải có ít nhất 3 ký tự")
    private String email;

    @NotBlank(message = "Email không được để trống")
    private String password;

    @NotBlank(message = "Tên hiển thị không được để trống")
    private String displayName;

    @NotBlank(message = "Loại tiền không được để trống")
    private String currency;
}