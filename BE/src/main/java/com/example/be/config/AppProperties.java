package com.example.be.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        String mailFrom,
        Jwt jwt,
        AutoDev autodev,
        Admin admin
) {
    public record Jwt(String secret, Duration expiration) {
    }

    public record AutoDev(String baseUrl, String apiKey) {
    }

    public record Admin(String nome, String cognome, String username, String email, String password) {
    }
}
