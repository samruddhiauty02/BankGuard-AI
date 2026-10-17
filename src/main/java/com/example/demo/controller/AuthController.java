package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.User;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -------- REGISTER --------
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return authService.register(user.getEmail(), user.getPassword());
    }

    // -------- LOGIN --------
    @PostMapping("/login")
    public String login(@RequestBody User user, HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        return authService.login(
                user.getEmail(),
                user.getPassword(),
                ip
        );
    }
}