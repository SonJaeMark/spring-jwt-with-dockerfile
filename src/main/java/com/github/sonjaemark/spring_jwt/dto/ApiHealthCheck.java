package com.github.sonjaemark.spring_jwt.dto;

public class ApiHealthCheck {
    private String status;

    public ApiHealthCheck(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
