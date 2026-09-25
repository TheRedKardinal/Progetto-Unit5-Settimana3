package com.example.be.dto.auth;

import com.example.be.validation.MaxBytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token mancante")
        String token,

        @NotBlank(message = "La nuova password è obbligatoria")
        @Size(min = 8, max = 64, message = "La password deve avere tra 8 e 64 caratteri")
        @MaxBytes(value = 72, message = "La password contiene troppi caratteri speciali: accorciala")
        String nuovaPassword
) {
}
