package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -------- REGISTRATION --------
    public String register(String email, String password) {

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return "User already exists";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);

        userRepository.save(user);

        return "User registered successfully";
    }

    // -------- LOGIN + ATO DETECTION --------
    public String login(String email, String password, String ipAddress) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();

        // 🚫 Account locked check
        if (user.isAccountLocked()) {
            return "Account locked due to suspicious activity";
        }

        // ❌ Wrong password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 3) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);
            return "Invalid credentials";
        }

        // 🔍 ATO Detection: New IP
        if (user.getLastLoginIp() != null && !user.getLastLoginIp().equals(ipAddress)) {
            user.setAccountLocked(true);
            userRepository.save(user);
            return "⚠️ Suspicious login detected! Account locked.";
        }

        // 🔍 ATO Detection: Too frequent login
        if (user.getLastLoginTime() != null) {
            long hours = java.time.Duration
                    .between(user.getLastLoginTime(), LocalDateTime.now())
                    .toHours();

            if (hours < 1) {
                return "⚠️ Too many login attempts in short time!";
            }
        }

        // ✅ Successful login
        user.setFailedLoginAttempts(0);
        user.setLastLoginIp(ipAddress);
        user.setLastLoginTime(LocalDateTime.now());

        userRepository.save(user);

        return "Login successful";
    }
}