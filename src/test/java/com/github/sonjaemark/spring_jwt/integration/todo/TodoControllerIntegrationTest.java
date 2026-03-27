package com.github.sonjaemark.spring_jwt.integration.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sonjaemark.spring_jwt.dto.TodoResponse;
import com.github.sonjaemark.spring_jwt.todo.TodoController;
import com.github.sonjaemark.spring_jwt.todo.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TodoService todoService;

    @Test
    void healthCheckShouldReturnOkStatus() throws Exception {
        mockMvc.perform(get("/api/todos/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void createShouldReturnCreatedTodo() throws Exception {
        TodoResponse response = TodoResponse.builder()
                .id(1L)
                .task("Write integration tests")
                .username("sonjaemark")
                .isDone(false)
                .createdAt(LocalDateTime.of(2026, 3, 28, 9, 30))
                .build();

        when(todoService.createTask(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/todos/v1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TodoPayload("Write integration tests"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.task").value("Write integration tests"))
                .andExpect(jsonPath("$.username").value("sonjaemark"))
                .andExpect(jsonPath("$.done").value(false));
    }

    @Test
    void getAllShouldReturnTodoList() throws Exception {
        TodoResponse response = TodoResponse.builder()
                .id(2L)
                .task("Read test results")
                .username("sonjaemark")
                .isDone(true)
                .createdAt(LocalDateTime.of(2026, 3, 28, 10, 0))
                .build();

        when(todoService.getAllTasks()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/todos/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].task").value("Read test results"))
                .andExpect(jsonPath("$[0].done").value(true));
    }

    @Test
    void deleteShouldReturnOk() throws Exception {
        doNothing().when(todoService).deleteTask(anyLong());

        mockMvc.perform(delete("/api/todos/v1/delete/1"))
                .andExpect(status().isOk());
    }

    private record TodoPayload(String task) {
    }
}
