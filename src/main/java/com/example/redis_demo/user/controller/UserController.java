package com.example.redis_demo.user.controller;

import com.example.redis_demo.common.enums.ResponseCode;
import com.example.redis_demo.common.response.ApiResponse;
import com.example.redis_demo.user.dto.request.LoginRequest;
import com.example.redis_demo.user.dto.request.RegisterRequest;
import com.example.redis_demo.user.dto.request.VerifyRequest;
import com.example.redis_demo.user.dto.response.LoginResponse;
import com.example.redis_demo.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);

        return ApiResponse.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .status(ResponseCode.SUCCESS.getStatus().value())
                .result(null)
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<String> verify(@Valid @RequestBody VerifyRequest request) {
        userService.verify(request);

        return ApiResponse.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .status(ResponseCode.SUCCESS.getStatus().value())
                .result(null)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);

        return ApiResponse.<LoginResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .status(ResponseCode.SUCCESS.getStatus().value())
                .result(new LoginResponse(token))
                .build();
    }
}
