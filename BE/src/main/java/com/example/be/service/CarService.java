package com.example.be.service;

import com.example.be.dto.PageResponse;
import com.example.be.dto.car.CarFilter;
import com.example.be.dto.car.CarRequest;
import com.example.be.dto.car.CarResponse;
import com.example.be.dto.car.CarSummaryResponse;
import com.example.be.entity.Car;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.event.PrezzoCambiatoEvent;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.NotFoundException;
import com.example.be.repository.CarRepository;
import com.example.be.repository.CarSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    static final Set<String> ORDINAMENTI_PUBBLICI = Set.of("prezzo", "chilometraggio", "anno", "publishedAt");
    static final Set<String> ORDINAMENTI_ADMIN = Set.of(
            "prezzo", "chilometraggio", "anno", "publishedAt", "createdAt", "updatedAt", "titolo", "statoAnnuncio");

    private final CarRepository carRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** Catalogo pubblico: solo annunci PUBBLICATO, qualunque stato arrivi nel filtro. */
    @Transactional(readOnly = true)
    public PageResponse<CarSummaryResponse> cercaPubblicati(CarFilter filtro, Pageable pageable) {
        validaPrezzi(filtro);
        CarFilter soloPubblicati = new CarFilter(filtro.q(), filtro.carburante(), filtro.condizione(),
                filtro.prezzoMin(), filtro.prezzoMax(), StatoAnnuncio.PUBBLICATO);

        Pageable pagina = Ordinamento.valida(pageable, ORDINAMENTI_PUBBLICI);
        return PageResponse.from(
                carRepository.findAll(CarSpecifications.da(soloPubblicati), pagina).map(CarSummaryResponse::from));
    }

    /** Dettaglio pubblico: un annuncio in bozza risulta inesistente. */
    @Transactional(readOnly = true)
    public CarResponse dettaglioPubblicato(UUID id) {
        return carRepository.findByIdAndStatoAnnuncio(id, StatoAnnuncio.PUBBLICATO)
                .map(CarResponse::from)
                .orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
    }

    // ---------------------------------------------------------------- Area admin

    /** Tutti gli annunci, bozze comprese; filtro facoltativo per stato. */
    @Transactional(readOnly = true)
    public PageResponse<CarSummaryResponse> cercaAdmin(CarFilter filtro, Pageable pageable) {
        validaPrezzi(filtro);
        Pageable pagina = Ordinamento.valida(pageable, ORDINAMENTI_ADMIN);
        return PageResponse.from(
                carRepository.findAll(CarSpecifications.da(filtro), pagina).map(CarSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public CarResponse dettaglioAdmin(UUID id) {
        return CarResponse.from(trova(id));
    }

    /** I nuovi annunci nascono sempre in bozza. */
    @Transactional
    public CarResponse crea(CarRequest req) {
        Car car = new Car();
        applica(car, req);
        car.setStatoAnnuncio(StatoAnnuncio.BOZZA);
        return CarResponse.from(carRepository.saveAndFlush(car));
    }

    @Transactional
    public CarResponse aggiorna(UUID id, CarRequest req) {
        Car car = trova(id);
        BigDecimal prezzoPrecedente = car.getPrezzo();
        applica(car, req);
        notificaSePrezzoCambiato(car, prezzoPrecedente);
        return CarResponse.from(carRepository.saveAndFlush(car));
    }

    @Transactional
    public CarResponse aggiornaPrezzo(UUID id, BigDecimal nuovoPrezzo) {
        Car car = trova(id);
        BigDecimal prezzoPrecedente = car.getPrezzo();
        car.setPrezzo(nuovoPrezzo.setScale(2));
        notificaSePrezzoCambiato(car, prezzoPrecedente);
        return CarResponse.from(carRepository.saveAndFlush(car));
    }

    /** Idempotente: pubblicare un annuncio già pubblicato non cambia nulla. */
    @Transactional
    public CarResponse pubblica(UUID id) {
        Car car = trova(id);
        if (!car.isPubblicato()) {
            car.setStatoAnnuncio(StatoAnnuncio.PUBBLICATO);
            car.setPublishedAt(Instant.now());
            // Mentre era in bozza il prezzo può essere cambiato: si ricontrollano le soglie
            eventPublisher.publishEvent(new PrezzoCambiatoEvent(car.getId(), car.getPrezzo()));
        }
        return CarResponse.from(carRepository.saveAndFlush(car));
    }

    /** Riporta in bozza: l'annuncio sparisce dal catalogo pubblico. */
    @Transactional
    public CarResponse bozza(UUID id) {
        Car car = trova(id);
        if (car.isPubblicato()) {
            car.setStatoAnnuncio(StatoAnnuncio.BOZZA);
            car.setPublishedAt(null);
        }
        return CarResponse.from(carRepository.saveAndFlush(car));
    }

    private Car trova(UUID id) {
        return carRepository.findById(id).orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
    }

    private void notificaSePrezzoCambiato(Car car, BigDecimal prezzoPrecedente) {
        // compareTo: 15000 e 15000.00 sono lo stesso prezzo
        if (car.isPubblicato() && car.getPrezzo().compareTo(prezzoPrecedente) != 0) {
            eventPublisher.publishEvent(new PrezzoCambiatoEvent(car.getId(), car.getPrezzo()));
        }
    }

    private static void applica(Car car, CarRequest req) {
        int annoMassimo = Year.now().getValue() + 1;
        if (req.anno() > annoMassimo) {
            throw new BadRequestException("L'anno non può essere successivo al " + annoMassimo);
        }
        car.setVin(req.vin() == null || req.vin().isBlank() ? null : req.vin().trim().toUpperCase(Locale.ROOT));
        car.setMarca(req.marca().trim());
        car.setModello(req.modello().trim());
        car.setAnno(Year.of(req.anno()));
        car.setTitolo(req.titolo().trim());
        car.setDescrizione(req.descrizione().trim());
        // Stessa scala delle colonne DB, così la risposta è coerente con i dati letti in seguito
        car.setChilometraggio(req.chilometraggio().setScale(1));
        car.setCarburante(req.carburante());
        car.setPrezzo(req.prezzo().setScale(2));
        car.setCondizione(req.condizione());

        car.getImmagini().clear();
        if (req.immagini() != null) {
            req.immagini().stream().map(String::trim).distinct().forEach(car.getImmagini()::add);
        }
    }

    // ---------------------------------------------------------------- Validazioni comuni

    static void validaPrezzi(CarFilter f) {
        if (f.prezzoMin() != null && f.prezzoMin().signum() < 0
                || f.prezzoMax() != null && f.prezzoMax().signum() < 0) {
            throw new BadRequestException("I prezzi non possono essere negativi");
        }
        BigDecimal min = f.prezzoMin();
        BigDecimal max = f.prezzoMax();
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("Il prezzo minimo non può superare il prezzo massimo");
        }
    }
}
