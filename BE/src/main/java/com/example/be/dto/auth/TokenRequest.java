package com.example.be.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Token ricevuto via email (es. verifica email). */
public record TokenRequest(
        @NotBlank(message = "Token mancante")
        String token
) {
}
