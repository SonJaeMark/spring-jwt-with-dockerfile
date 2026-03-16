package com.github.sonjaemark.spring_jwt.dto;

public class TodoRequest {
    private String task;

    public TodoRequest() {}

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

}
