package com.example.be.service;

import com.example.be.client.AutoDevClient;
import com.example.be.dto.car.VinLookupResponse;
import com.example.be.entity.Carburante;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VinLookupService {

    public static final String CACHE = "vin-lookup";

    private static final Pattern VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private final AutoDevClient autoDevClient;

    /**
     * Dati e foto dell'auto da auto.dev. Il risultato viene messo in cache per VIN
     * (le eccezioni no): ripetere la ricerca non consuma altre chiamate della quota gratuita.
     * Chiamato sempre con il VIN già normalizzato da {@link #normalizza(String)}.
     */
    @Cacheable(CACHE)
    public VinLookupResponse cerca(String vin) {
        AutoDevClient.VinDecode dati = autoDevClient.decodificaVin(vin)
                .orElseThrow(() -> new NotFoundException("Nessun dato trovato su auto.dev per il VIN " + vin));

        // Le foto si chiedono solo se il VIN esiste: una ricerca fallita costa una sola chiamata
        List<String> immagini = autoDevClient.foto(vin);

        String modello = dati.model();
        if (dati.trim() != null && !dati.trim().isBlank() && modello != null) {
            modello = modello + " " + dati.trim();
        }
        return new VinLookupResponse(vin, dati.make(), modello, dati.year(), dati.engine(),
                deduciCarburante(dati.engine()), immagini);
    }

    public static String normalizza(String vin) {
        String v = vin == null ? "" : vin.trim().toUpperCase(Locale.ROOT);
        if (!VIN.matcher(v).matches()) {
            throw new BadRequestException("VIN non valido: deve avere 17 caratteri alfanumerici (senza I, O, Q)");
        }
        return v;
    }

    /** Deduzione prudente dalla descrizione del motore; null se non è chiara (l'admin sceglie a mano). */
    static Carburante deduciCarburante(String motore) {
        if (motore == null || motore.isBlank()) {
            return null;
        }
        String m = motore.toLowerCase(Locale.ROOT);
        if (m.contains("electric") && !m.contains("hybrid") || m.matches(".*\\bev\\b.*")) {
            return Carburante.ELETTRICA;
        }
        if (m.contains("hybrid") || m.contains("phev") || m.contains("hev")) {
            return Carburante.IBRIDA;
        }
        if (m.contains("diesel") || m.contains("tdi") || m.contains("crdi") || m.contains("cdi")) {
            return Carburante.DIESEL;
        }
        if (m.contains("cng") || m.contains("natural gas")) {
            return Carburante.METANO;
        }
        if (m.contains("lpg")) {
            return Carburante.GPL;
        }
        if (m.contains("gas") || m.contains("petrol")) {
            return Carburante.BENZINA;
        }
        return null;
    }
}
