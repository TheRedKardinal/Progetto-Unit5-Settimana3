package com.example.be.dto.car;

import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Versione ridotta per le liste (card del catalogo, tabella admin): niente descrizione, solo la copertina. */
public record CarSummaryResponse(
        UUID id,
        String marca,
        String modello,
        Integer anno,
        String titolo,
        BigDecimal chilometraggio,
        Carburante carburante,
        BigDecimal prezzo,
        Condizione condizione,
        StatoAnnuncio statoAnnuncio,
        String copertina,
        Instant publishedAt,
        Instant updatedAt
) {
    public static CarSummaryResponse from(Car car) {
        return new CarSummaryResponse(
                car.getId(),
                car.getMarca(),
                car.getModello(),
                car.getAnno() == null ? null : car.getAnno().getValue(),
                car.getTitolo(),
                car.getChilometraggio(),
                car.getCarburante(),
                car.getPrezzo(),
                car.getCondizione(),
                car.getStatoAnnuncio(),
                car.getImmagini().isEmpty() ? null : car.getImmagini().getFirst(),
                car.getPublishedAt(),
                car.getUpdatedAt()
        );
    }
}
