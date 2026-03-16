package com.github.sonjaemark.spring_jwt.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
    

    public LoginRequest() {}


}