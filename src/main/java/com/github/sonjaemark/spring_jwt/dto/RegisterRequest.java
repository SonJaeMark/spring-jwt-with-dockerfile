package com.github.sonjaemark.spring_jwt.dto;

import com.github.sonjaemark.spring_jwt.user.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public RegisterRequest() {}

}