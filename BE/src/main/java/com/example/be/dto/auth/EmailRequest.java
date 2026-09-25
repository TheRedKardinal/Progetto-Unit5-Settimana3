package com.example.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        String email
) {
}
