package com.example.redis_demo.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    @Builder.Default
    private int status = 1000;
    private String code;
    private String message;
    private LocalDateTime timestamp;

    private T result;
}