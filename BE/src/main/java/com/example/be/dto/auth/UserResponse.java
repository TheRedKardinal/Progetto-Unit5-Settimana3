package com.example.be.dto.auth;

import com.example.be.entity.Role;
import com.example.be.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nome,
        String cognome,
        String username,
        String email,
        List<String> ruoli,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getCognome(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getRole).sorted().toList(),
                user.getCreatedAt()
        );
    }
}
