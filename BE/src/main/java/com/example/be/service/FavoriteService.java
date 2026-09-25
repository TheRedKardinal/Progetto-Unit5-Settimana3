package com.example.be.service;

import com.example.be.dto.favorite.FavoriteResponse;
import com.example.be.entity.Car;
import com.example.be.entity.Favorite;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.entity.User;
import com.example.be.event.PrezzoCambiatoEvent;
import com.example.be.exception.NotFoundException;
import com.example.be.repository.CarRepository;
import com.example.be.repository.FavoriteRepository;
import com.example.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<FavoriteResponse> elenco(UUID userId) {
        return favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    /** Idempotente: se l'auto è già tra i preferiti restituisce quello esistente. Solo annunci pubblicati. */
    @Transactional
    public FavoriteResponse aggiungi(UUID userId, UUID carId) {
        return favoriteRepository.findByUserIdAndCarId(userId, carId)
                .map(FavoriteResponse::from)
                .orElseGet(() -> {
                    Car car = carRepository.findByIdAndStatoAnnuncio(carId, StatoAnnuncio.PUBBLICATO)
                            .orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
                    User user = userRepository.getReferenceById(userId);
                    return FavoriteResponse.from(favoriteRepository.saveAndFlush(new Favorite(user, car)));
                });
    }

    /** Idempotente: rimuovere un preferito inesistente non è un errore. */
    @Transactional
    public void rimuovi(UUID userId, UUID carId) {
        favoriteRepository.findByUserIdAndCarId(userId, carId).ifPresent(favoriteRepository::delete);
    }

    /**
     * Imposta (o rimuove, con null) la soglia e azzera notificatoAt.
     * Se il prezzo attuale è già sotto la nuova soglia la notifica parte subito.
     */
    @Transactional
    public FavoriteResponse impostaSoglia(UUID userId, UUID carId, BigDecimal soglia) {
        Favorite favorite = favoriteRepository.findByUserIdAndCarId(userId, carId)
                .orElseThrow(() -> new NotFoundException("L'auto non è tra i tuoi preferiti"));

        favorite.setSogliaPrezzo(soglia == null ? null : soglia.setScale(2));
        favorite.setNotificatoAt(null);

        Car car = favorite.getCar();
        if (soglia != null && car.isPubblicato() && car.getPrezzo().compareTo(soglia) < 0) {
            eventPublisher.publishEvent(new PrezzoCambiatoEvent(car.getId(), car.getPrezzo()));
        }
        return FavoriteResponse.from(favorite);
    }
}
