package com.github.sonjaemark.spring_jwt.auth;

import com.github.sonjaemark.spring_jwt.dto.AuthResponse;
import com.github.sonjaemark.spring_jwt.dto.LoginRequest;
import com.github.sonjaemark.spring_jwt.dto.RegisterRequest;
import com.github.sonjaemark.spring_jwt.token.RefreshToken;
import com.github.sonjaemark.spring_jwt.token.RefreshTokenRepository;
import com.github.sonjaemark.spring_jwt.user.Role;
import com.github.sonjaemark.spring_jwt.user.User;
import com.github.sonjaemark.spring_jwt.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow();

        String accessToken =
            jwtService.generateToken(user.getId(), user.getUsername());

        RefreshToken refreshToken =
            refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
    }

    public AuthResponse register(RegisterRequest request) {

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole() != null ? request.getRole() : Role.USER)
            .build();

        userRepository.save(user);

        return AuthResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .role(user.getRole().name())
            .build();
    }

    public AuthResponse refreshToken(String requestToken) {

        RefreshToken refreshToken =
            refreshTokenRepository.findByToken(requestToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String accessToken =
            jwtService.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(
            accessToken,
            requestToken,
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
    }

    public void logout(String refreshToken) {
        refreshTokenRepository
            .findByToken(refreshToken)
            .ifPresent(refreshTokenRepository::delete);
    }
}