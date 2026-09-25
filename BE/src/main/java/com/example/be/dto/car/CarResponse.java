package com.example.be.dto.car;

import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Dettaglio completo di un annuncio. */
public record CarResponse(
        UUID id,
        String vin,
        String marca,
        String modello,
        Integer anno,
        String titolo,
        String descrizione,
        BigDecimal chilometraggio,
        Carburante carburante,
        BigDecimal prezzo,
        Condizione condizione,
        StatoAnnuncio statoAnnuncio,
        List<String> immagini,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt
) {
    public static CarResponse from(Car car) {
        return new CarResponse(
                car.getId(),
                car.getVin(),
                car.getMarca(),
                car.getModello(),
                car.getAnno() == null ? null : car.getAnno().getValue(),
                car.getTitolo(),
                car.getDescrizione(),
                car.getChilometraggio(),
                car.getCarburante(),
                car.getPrezzo(),
                car.getCondizione(),
                car.getStatoAnnuncio(),
                List.copyOf(car.getImmagini()),
                car.getCreatedAt(),
                car.getUpdatedAt(),
                car.getPublishedAt()
        );
    }
}
