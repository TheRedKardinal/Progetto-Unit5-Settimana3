package com.example.be.dto.favorite;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** sogliaPrezzo null = rimuove la soglia (nessuna notifica). */
public record SogliaRequest(
        @Positive(message = "La soglia deve essere maggiore di zero")
        @Digits(integer = 10, fraction = 2, message = "Soglia non valida (max 2 decimali)")
        BigDecimal sogliaPrezzo
) {
}
