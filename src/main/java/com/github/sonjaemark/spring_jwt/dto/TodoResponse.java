package com.github.sonjaemark.spring_jwt.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoResponse {
    private Long id;
    private String task;
    private boolean isDone;
    private LocalDateTime createdAt;
    private String username;
}
