package com.example.be.config;

import com.example.be.client.AutoDevClient;
import com.example.be.client.AutoDevClient.Listing;
import com.example.be.client.AutoDevClient.ListingVehicle;
import com.example.be.client.AutoDevClient.RetailListing;
import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.exception.ServizioEsternoException;
import com.example.be.repository.CarRepository;
import com.example.be.service.VinLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Popola il catalogo con annunci reali di auto di lusso presi da auto.dev (GET /listings), già pubblicati.
 * Parte solo con app.catalog-seed.enabled=true e se il catalogo ha meno di app.catalog-seed.size annunci;
 * i VIN già presenti vengono saltati. Un errore di auto.dev non blocca l'avvio dell'applicazione.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogSeeder implements ApplicationRunner {

    /** Massimo consentito dal piano gratuito di auto.dev. */
    static final int PER_PAGINA = 20;
    private static final BigDecimal KM_PER_MIGLIO = new BigDecimal("1.609344");

    private final CatalogSeedProperties props;
    private final AutoDevClient autoDevClient;
    private final CarRepository carRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!props.enabled()) {
            return;
        }
        long presenti = carRepository.count();
        if (presenti >= props.size()) {
            log.info("Catalogo iniziale non necessario: già {} annunci", presenti);
            return;
        }
        try {
            int creati = popola(props.size());
            log.info("Catalogo iniziale: {} annunci importati da auto.dev", creati);
        } catch (ServizioEsternoException e) {
            log.warn("Catalogo iniziale non importato: {}", e.getMessage());
        }
    }

    private int popola(int obiettivo) {
        List<Car> nuove = new ArrayList<>();
        // Una pagina in più del minimo, per compensare gli annunci scartati (senza foto, VIN doppi, ...)
        int maxPagine = (obiettivo + PER_PAGINA - 1) / PER_PAGINA + 1;
        for (int pagina = 1; pagina <= maxPagine && nuove.size() < obiettivo; pagina++) {
            List<Listing> annunci = autoDevClient.annunci(props.marche(), props.fasciaPrezzo(), pagina, PER_PAGINA);
            for (Listing annuncio : annunci) {
                if (nuove.size() >= obiettivo) {
                    break;
                }
                daAnnuncio(annuncio)
                        .filter(car -> nuove.stream().noneMatch(n -> n.getVin().equals(car.getVin())))
                        .filter(car -> !carRepository.existsByVin(car.getVin()))
                        .ifPresent(nuove::add);
            }
            if (annunci.size() < PER_PAGINA) {
                break;
            }
        }
        carRepository.saveAll(nuove);
        return nuove.size();
    }

    /** Converte un annuncio auto.dev in un'auto pubblicata; vuoto se mancano dati indispensabili. */
    static Optional<Car> daAnnuncio(Listing annuncio) {
        ListingVehicle v = annuncio.vehicle();
        RetailListing r = annuncio.retailListing();
        String vin = annuncio.vin();
        if (vin == null || vin.length() != 17 || v == null || r == null || v.year() == null
                || isBlank(v.make()) || isBlank(v.model()) || r.price() == null || r.price().signum() <= 0
                || isBlank(r.primaryImage()) || !r.primaryImage().startsWith("http")) {
            return Optional.empty();
        }

        BigDecimal km = r.miles() == null ? BigDecimal.ZERO
                : r.miles().multiply(KM_PER_MIGLIO).setScale(0, RoundingMode.HALF_UP);

        Car car = new Car();
        car.setVin(vin.toUpperCase(Locale.ROOT));
        car.setMarca(tronca(v.make(), 60));
        car.setModello(tronca(v.model(), 80));
        car.setAnno(Year.of(v.year()));
        car.setTitolo(tronca(titolo(v), 150));
        car.setDescrizione(descrizione(v, km));
        car.setChilometraggio(km.setScale(1, RoundingMode.HALF_UP));
        car.setCarburante(carburante(v));
        // Prezzo di listino USA arrotondato alle centinaia e usato come importo in euro
        car.setPrezzo(r.price().divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.UNNECESSARY));
        car.setCondizione(condizione(km));
        car.setImmagini(new ArrayList<>(List.of(r.primaryImage())));
        car.setStatoAnnuncio(StatoAnnuncio.PUBBLICATO);
        car.setPublishedAt(Instant.now());
        return Optional.of(car);
    }

    private static String titolo(ListingVehicle v) {
        String base = v.make() + " " + v.model();
        return isBlank(v.trim()) ? base : base + " " + v.trim();
    }

    private static String descrizione(ListingVehicle v, BigDecimal km) {
        StringJoiner testo = new StringJoiner("\n");
        testo.add(v.year() + " " + titolo(v) + ", " + String.format(Locale.ITALIAN, "%,d", km.longValue()) + " km.");
        if (!isBlank(v.engine())) {
            testo.add("Motore: " + v.engine() + ".");
        }
        if (!isBlank(v.transmission())) {
            testo.add("Cambio: " + v.transmission() + ".");
        }
        if (!isBlank(v.drivetrain())) {
            testo.add("Trazione: " + v.drivetrain() + ".");
        }
        if (!isBlank(v.exteriorColor()) || !isBlank(v.interiorColor())) {
            testo.add("Colori: " + (isBlank(v.exteriorColor()) ? "—" : v.exteriorColor())
                    + " / interni " + (isBlank(v.interiorColor()) ? "—" : v.interiorColor()) + ".");
        }
        testo.add("Dati e foto dell'annuncio originale forniti da auto.dev.");
        return testo.toString();
    }

    /** Carburante dal campo fuel (o dal motore); benzina se non deducibile: sulle auto di lusso è il caso più comune. */
    private static Carburante carburante(ListingVehicle v) {
        String indizi = (v.fuel() == null ? "" : v.fuel()) + " " + (v.engine() == null ? "" : v.engine());
        Carburante c = VinLookupService.deduciCarburante(indizi);
        return c != null ? c : Carburante.BENZINA;
    }

    /** L'annuncio non dice se l'auto è nuova: lo si ricava dai chilometri. */
    private static Condizione condizione(BigDecimal km) {
        if (km.compareTo(BigDecimal.valueOf(100)) < 0) {
            return Condizione.NUOVO;
        }
        if (km.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return Condizione.KM_0;
        }
        return Condizione.USATO;
    }

    private static String tronca(String s, int max) {
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max).trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
