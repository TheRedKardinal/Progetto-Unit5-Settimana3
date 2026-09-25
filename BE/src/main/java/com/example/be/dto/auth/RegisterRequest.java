package com.example.be.dto.auth;

import com.example.be.validation.MaxBytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 100, message = "Il nome può avere al massimo 100 caratteri")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 100, message = "Il cognome può avere al massimo 100 caratteri")
        String cognome,

        @NotBlank(message = "Lo username è obbligatorio")
        @Size(min = 3, max = 50, message = "Lo username deve avere tra 3 e 50 caratteri")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "Lo username può contenere solo lettere, numeri, punto, trattino e underscore")
        String username,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        @Size(max = 255, message = "Email troppo lunga")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, max = 64, message = "La password deve avere tra 8 e 64 caratteri")
        @MaxBytes(value = 72, message = "La password contiene troppi caratteri speciali: accorciala")
        String password
) {
}
