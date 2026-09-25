package com.example.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token mancante")
        String token,

        @NotBlank(message = "La nuova password è obbligatoria")
        @Size(min = 8, max = 16, message = "La password deve avere tra 8 e 16 caratteri")
        String nuovaPassword
) {
}
