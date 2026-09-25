package com.example.be.dto.car;

import com.example.be.entity.Carburante;

import java.util.List;

/**
 * Dati per precompilare il form annuncio a partire dal VIN.
 * carburante è null quando non si può dedurre con certezza dalla descrizione del motore.
 */
public record VinLookupResponse(
        String vin,
        String marca,
        String modello,
        Integer anno,
        String motore,
        Carburante carburante,
        List<String> immagini
) {
}
