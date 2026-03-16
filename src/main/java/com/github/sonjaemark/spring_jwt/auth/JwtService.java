package com.github.sonjaemark.spring_jwt.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private static final String SECRET =
    "super-secret-key-super-secret-key-super-secret";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(Long userId, String username) {

        return Jwts.builder()
        .claim("userId", userId)
        .subject(username)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
        .signWith(getKey())
        .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public Long extractUserId(String token) {

        Object userId = Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("userId");

        return Long.valueOf(userId.toString());
    }

}