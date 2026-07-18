package com.example.redis_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoRequest {
    @NotBlank(message = "Task không được để trống")
    @Size(min = 3, message = "Task phải có ít nhất 3 ký tự")
    private String task;
}
