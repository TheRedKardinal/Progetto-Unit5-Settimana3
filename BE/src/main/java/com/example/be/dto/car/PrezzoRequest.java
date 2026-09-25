package com.example.be.dto.car;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PrezzoRequest(
        @NotNull(message = "Il prezzo è obbligatorio")
        @Positive(message = "Il prezzo deve essere maggiore di zero")
        @Digits(integer = 10, fraction = 2, message = "Prezzo non valido (max 2 decimali)")
        BigDecimal prezzo
) {
}
