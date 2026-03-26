package com.github.sonjaemark.spring_jwt.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.
AuthenticationManager;
import org.springframework.security.config.annotation.authentication.
configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public ApplicationRunner environmentVariableLogger(Environment environment) {
        return args -> {
            log.info("Startup env check: DB_URL present = {}", hasText(environment.getProperty("DB_URL")));
            log.info("Startup env check: DB_USER present = {}", hasText(environment.getProperty("DB_USER")));
            log.info("Startup env check: DB_PASSWORD present = {}", hasText(environment.getProperty("DB_PASSWORD")));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
