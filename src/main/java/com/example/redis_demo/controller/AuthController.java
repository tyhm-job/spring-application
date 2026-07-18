package com.example.redis_demo.controller;

import com.example.redis_demo.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;

    public AuthController(JwtService jwtService) { this.jwtService = jwtService; }

    @GetMapping("/login")
    public String login(@RequestParam String username) {
        return jwtService.generateToken(username);
    }
}