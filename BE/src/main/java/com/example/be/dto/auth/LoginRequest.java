package com.example.be.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Inserisci email o username")
        String emailOrUsername,

        @NotBlank(message = "Inserisci la password")
        String password
) {
}
