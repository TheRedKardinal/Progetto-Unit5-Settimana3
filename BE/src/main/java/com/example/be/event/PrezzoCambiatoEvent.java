package com.example.be.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Emesso quando cambia il prezzo di un annuncio pubblicato o quando un annuncio viene pubblicato.
 * Il listener delle soglie di prezzo lo gestisce dopo il commit.
 */
public record PrezzoCambiatoEvent(UUID carId, BigDecimal nuovoPrezzo) {
}
