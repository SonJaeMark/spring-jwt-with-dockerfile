package com.github.sonjaemark.spring_jwt.todo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.sonjaemark.spring_jwt.dto.TodoRequest;
import com.github.sonjaemark.spring_jwt.dto.TodoResponse;

@RestController
@RequestMapping("/api/todos/v1")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @PostMapping("/create")
    public TodoResponse create(@RequestBody TodoRequest request) {
        return todoService.createTask(request.getTask());
    }

    @GetMapping
    public List<TodoResponse> getAll() {
        return todoService.getAllTasks();
    }

    @PutMapping("/update/{id}")
    public TodoResponse update(
            @PathVariable Long id,
            @RequestBody TodoRequest request) {

        return todoService.updateTask(id, request.getTask());
    }

    @PutMapping("/done/{id}")
    public TodoResponse markDone(@PathVariable Long id) {

        return todoService.markAsDone(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {

        todoService.deleteTask(id);
    }
}