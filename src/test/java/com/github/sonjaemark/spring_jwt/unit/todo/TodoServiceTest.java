package com.github.sonjaemark.spring_jwt.unit.todo;

import com.github.sonjaemark.spring_jwt.dto.TodoResponse;
import com.github.sonjaemark.spring_jwt.todo.Todo;
import com.github.sonjaemark.spring_jwt.todo.TodoRepository;
import com.github.sonjaemark.spring_jwt.todo.TodoService;
import com.github.sonjaemark.spring_jwt.user.Role;
import com.github.sonjaemark.spring_jwt.user.User;
import com.github.sonjaemark.spring_jwt.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTaskShouldSaveTodoForAuthenticatedUser() {
        User user = User.builder()
                .id(1L)
                .username("sonjaemark")
                .role(Role.USER)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sonjaemark", null)
        );

        when(userRepository.findByUsername("sonjaemark")).thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoResponse response = todoService.createTask("Finish tests");

        ArgumentCaptor<Todo> todoCaptor = ArgumentCaptor.forClass(Todo.class);
        verify(todoRepository).save(todoCaptor.capture());

        Todo savedTodo = todoCaptor.getValue();
        assertEquals("Finish tests", savedTodo.getTask());
        assertEquals(user, savedTodo.getUser());
        assertFalse(savedTodo.getIsDone());
        assertEquals("sonjaemark", response.getUsername());
        assertEquals("Finish tests", response.getTask());
        assertFalse(response.isDone());
    }

    @Test
    void updateTaskShouldRejectTodosOwnedByAnotherUser() {
        User currentUser = User.builder()
                .id(1L)
                .username("sonjaemark")
                .role(Role.USER)
                .build();

        User differentUser = User.builder()
                .id(2L)
                .username("other-user")
                .role(Role.USER)
                .build();

        Todo todo = Todo.builder()
                .id(7L)
                .task("Existing task")
                .createdAt(LocalDateTime.now())
                .user(differentUser)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sonjaemark", null)
        );

        when(userRepository.findByUsername("sonjaemark")).thenReturn(Optional.of(currentUser));
        when(todoRepository.findById(7L)).thenReturn(Optional.of(todo));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> todoService.updateTask(7L, "Updated task"));

        assertEquals("Unauthorized", exception.getMessage());
    }
}
