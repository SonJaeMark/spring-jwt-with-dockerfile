package com.github.sonjaemark.spring_jwt.todo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.sonjaemark.spring_jwt.dto.TodoResponse;
import com.github.sonjaemark.spring_jwt.user.User;
import com.github.sonjaemark.spring_jwt.user.UserRepository;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private TodoResponse todoResponse;

    private User getCurrentUser() {

        String username = SecurityContextHolder
            .getContext()
            .getAuthentication()
        .getName();

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public TodoResponse createTask(String task) {

        User user = getCurrentUser();

        Todo todo = Todo.builder()
            .task(task)
            .createdAt(LocalDateTime.now())
            .user(user)
        .build();

        todoResponse = TodoResponse.builder()
            .task(task)
            .createdAt(LocalDateTime.now())
            .username(user.getUsername())
            .isDone(false)
        .build();

        todoRepository.save(todo);
        return todoResponse;
    }

    public List<TodoResponse> getAllTasks() {

        User user = getCurrentUser();

        return todoRepository.findByUser(user).stream()
            .map(todo -> TodoResponse.builder()
                .id(todo.getId())
                .task(todo.getTask())
                .isDone(todo.getIsDone())
                .createdAt(todo.getCreatedAt())
                .username(todo.getUser().getUsername())
                .build())
            .collect(Collectors.toList());
    }

    public TodoResponse updateTask(Long id, String task) {

        User user = getCurrentUser();

        Todo todo = todoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        todo.setTask(task);

        todoRepository.save(todo);

        return TodoResponse.builder()
            .id(todo.getId())
            .task(task)
            .isDone(todo.getIsDone())
            .createdAt(todo.getCreatedAt())
            .username(todo.getUser().getUsername())
        .build();
    }

    public TodoResponse markAsDone(Long id) {

        User user = getCurrentUser();

        Todo todo = todoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        todo.setIsDone(true);

        todoRepository.save(todo);

        return TodoResponse.builder()
            .id(todo.getId())
            .task(todo.getTask())
            .isDone(todo.getIsDone())
            .createdAt(todo.getCreatedAt())
            .username(todo.getUser().getUsername())
        .build();
    }

    public void deleteTask(Long id) {

        User user = getCurrentUser();

        Todo todo = todoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        todoRepository.delete(todo);
    }
}