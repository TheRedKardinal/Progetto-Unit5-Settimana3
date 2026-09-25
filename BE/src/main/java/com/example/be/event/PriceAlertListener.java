package com.example.be.event;

import com.example.be.entity.Car;
import com.example.be.entity.Favorite;
import com.example.be.repository.CarRepository;
import com.example.be.repository.FavoriteRepository;
import com.example.be.service.AfterCommit;
import com.example.be.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Notifica via email gli utenti che hanno l'auto tra i preferiti con una soglia superiore al nuovo prezzo.
 * Gira dopo il commit (solo se la modifica è stata salvata) e in un thread separato
 * (l'admin non aspetta l'invio delle email).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceAlertListener {

    private final CarRepository carRepository;
    private final FavoriteRepository favoriteRepository;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPrezzoCambiato(PrezzoCambiatoEvent event) {
        Car car = carRepository.findById(event.carId()).orElse(null);
        if (car == null || !car.isPubblicato()) {
            return;
        }
        // Prezzo attuale dal DB: se nel frattempo è cambiato di nuovo vale l'ultimo
        BigDecimal prezzo = car.getPrezzo();

        // Prezzo tornato pari o sopra la soglia: quegli utenti potranno essere avvisati di nuovo
        int riattivati = favoriteRepository.resetNotifiche(car.getId(), prezzo);

        List<Favorite> daNotificare = favoriteRepository.findDaNotificare(car.getId(), prezzo);
        Instant now = Instant.now();
        int inviate = 0;
        for (Favorite f : daNotificare) {
            // Solo chi "vince" l'update invia la mail: niente doppioni con eventi concorrenti
            if (favoriteRepository.marcaNotificato(f.getId(), now) == 1) {
                String email = f.getUser().getEmail();
                String nome = f.getUser().getNome();
                BigDecimal soglia = f.getSogliaPrezzo();
                AfterCommit.run(() -> emailService.inviaNotificaPrezzo(
                        email, nome, car.getId(), car.getTitolo(), prezzo, soglia));
                inviate++;
            }
        }
        log.info("Prezzo di '{}' = {}: {} notifiche, {} soglie riattivate", car.getTitolo(), prezzo, inviate, riattivati);
    }
}
