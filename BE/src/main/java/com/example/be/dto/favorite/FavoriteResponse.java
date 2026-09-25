package com.example.be.dto.favorite;

import com.example.be.dto.car.CarSummaryResponse;
import com.example.be.entity.Favorite;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Preferito dell'utente. car.statoAnnuncio può essere BOZZA se l'admin ha ritirato l'annuncio:
 * il frontend lo mostra come "non più disponibile".
 */
public record FavoriteResponse(
        CarSummaryResponse car,
        BigDecimal sogliaPrezzo,
        boolean prezzoSottoSoglia,
        Instant notificatoAt,
        Instant createdAt
) {
    public static FavoriteResponse from(Favorite f) {
        BigDecimal soglia = f.getSogliaPrezzo();
        boolean sotto = soglia != null && f.getCar().getPrezzo().compareTo(soglia) < 0;
        return new FavoriteResponse(
                CarSummaryResponse.from(f.getCar()),
                soglia,
                sotto,
                f.getNotificatoAt(),
                f.getCreatedAt()
        );
    }
}
