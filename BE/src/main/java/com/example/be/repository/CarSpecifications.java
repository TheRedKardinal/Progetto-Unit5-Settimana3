package com.example.be.repository;

import com.example.be.dto.car.CarFilter;
import com.example.be.entity.Car;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Costruisce la Specification di ricerca a partire dai filtri (quelli null vengono ignorati). */
public final class CarSpecifications {

    private static final char ESCAPE = '\\';

    private CarSpecifications() {
    }

    public static Specification<Car> da(CarFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicati = new ArrayList<>();

            if (f.statoAnnuncio() != null) {
                predicati.add(cb.equal(root.get("statoAnnuncio"), f.statoAnnuncio()));
            }
            if (f.carburante() != null) {
                predicati.add(cb.equal(root.get("carburante"), f.carburante()));
            }
            if (f.condizione() != null) {
                predicati.add(cb.equal(root.get("condizione"), f.condizione()));
            }
            if (f.prezzoMin() != null) {
                predicati.add(cb.greaterThanOrEqualTo(root.get("prezzo"), f.prezzoMin()));
            }
            if (f.prezzoMax() != null) {
                predicati.add(cb.lessThanOrEqualTo(root.get("prezzo"), f.prezzoMax()));
            }

            // Ricerca testuale: ogni parola deve comparire in titolo, marca o modello (case-insensitive)
            if (f.q() != null && !f.q().isBlank()) {
                for (String parola : f.q().trim().toLowerCase(Locale.ROOT).split("\\s+")) {
                    String pattern = "%" + escapeLike(parola) + "%";
                    predicati.add(cb.or(
                            cb.like(cb.lower(root.get("titolo")), pattern, ESCAPE),
                            cb.like(cb.lower(root.get("marca")), pattern, ESCAPE),
                            cb.like(cb.lower(root.get("modello")), pattern, ESCAPE)
                    ));
                }
            }

            return cb.and(predicati.toArray(Predicate[]::new));
        };
    }

    /** % e _ digitati dall'utente vanno cercati come caratteri, non come jolly. */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
