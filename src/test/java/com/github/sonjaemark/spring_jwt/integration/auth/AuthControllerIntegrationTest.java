package com.github.sonjaemark.spring_jwt.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sonjaemark.spring_jwt.auth.AuthController;
import com.github.sonjaemark.spring_jwt.auth.AuthService;
import com.github.sonjaemark.spring_jwt.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginShouldReturnAuthResponse() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .userId(1L)
                .username("sonjaemark")
                .role("USER")
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/todos/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload("sonjaemark", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("sonjaemark"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void logoutShouldReturnOk() throws Exception {
        doNothing().when(authService).logout("refresh-token");

        mockMvc.perform(post("/api/todos/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("refresh-token")))
                .andExpect(status().isOk());
    }

    private record LoginPayload(String username, String password) {
    }
}
