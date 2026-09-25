package com.example.be.dto.car;

import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;

import java.math.BigDecimal;

/**
 * Filtri di ricerca, tutti opzionali.
 * statoAnnuncio è usato solo dall'area admin: il catalogo pubblico forza sempre PUBBLICATO.
 */
public record CarFilter(
        String q,
        Carburante carburante,
        Condizione condizione,
        BigDecimal prezzoMin,
        BigDecimal prezzoMax,
        StatoAnnuncio statoAnnuncio
) {
}
