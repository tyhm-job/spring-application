package com.example.redis_demo.common.exception;

import com.example.redis_demo.common.enums.ResponseCode;
import com.example.redis_demo.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        ResponseCode responseCode = ex.getResponseCode();

        ApiResponse<?> response = ApiResponse.builder()
                .code(responseCode.getCode()) // Bây giờ là String (ví dụ "AUTH_001")
                .message(responseCode.getMessage())
                .status(responseCode.getStatus().value()) // Lấy giá trị int từ HttpStatus
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(responseCode.getStatus()).body(response);
    }

    // Bắt mọi lỗi chưa xác định (để tránh rò rỉ stack trace ra ngoài)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        exception.printStackTrace();

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code("SYS_9999") // Nên để theo format mã lỗi của bạn
                .message("Internal Server Error") // Tránh để lộ chi tiết exception cho client
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
}
