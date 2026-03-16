package com.github.sonjaemark.spring_jwt.auth;

import com.github.sonjaemark.spring_jwt.dto.AuthResponse;
import com.github.sonjaemark.spring_jwt.dto.LoginRequest;
import com.github.sonjaemark.spring_jwt.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody String refreshToken) {

        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody String refreshToken) {
        authService.logout(refreshToken);
    }

}