package com.example.be.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utility per ricavare l'id dell'utente loggato dal JWT
 * (nei controller: {@code @AuthenticationPrincipal Jwt jwt}).
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
