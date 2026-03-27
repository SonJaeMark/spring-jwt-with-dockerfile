package com.github.sonjaemark.spring_jwt.unit.auth;

import com.github.sonjaemark.spring_jwt.auth.AuthService;
import com.github.sonjaemark.spring_jwt.auth.JwtService;
import com.github.sonjaemark.spring_jwt.auth.RefreshTokenService;
import com.github.sonjaemark.spring_jwt.dto.AuthResponse;
import com.github.sonjaemark.spring_jwt.dto.LoginRequest;
import com.github.sonjaemark.spring_jwt.dto.RegisterRequest;
import com.github.sonjaemark.spring_jwt.token.RefreshToken;
import com.github.sonjaemark.spring_jwt.token.RefreshTokenRepository;
import com.github.sonjaemark.spring_jwt.user.Role;
import com.github.sonjaemark.spring_jwt.user.User;
import com.github.sonjaemark.spring_jwt.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldAuthenticateAndReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername("sonjaemark");
        request.setPassword("secret");

        User user = User.builder()
                .id(1L)
                .username("sonjaemark")
                .password("encoded-secret")
                .role(Role.USER)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(10L)
                .token("refresh-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        when(userRepository.findByUsername("sonjaemark")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "sonjaemark")).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authCaptor.capture());
        assertEquals("sonjaemark", authCaptor.getValue().getPrincipal());
        assertEquals("secret", authCaptor.getValue().getCredentials());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(1L, response.getUserId());
        assertEquals("USER", response.getRole());
    }

    @Test
    void registerShouldEncodePasswordAndDefaultRoleToUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-user");
        request.setEmail("new-user@example.com");
        request.setPassword("plain-password");

        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(5L);
            return savedUser;
        });

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("new-user", savedUser.getUsername());
        assertEquals("new-user@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertNotNull(response);
        assertEquals(5L, response.getUserId());
        assertEquals("new-user", response.getUsername());
        assertEquals("USER", response.getRole());
    }
}
