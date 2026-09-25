package com.example.be.exception;

import org.springframework.http.HttpStatus;

/** Errore di un servizio esterno (es. auto.dev): 502 se risponde male, 503 se non è disponibile. */
public class ServizioEsternoException extends ApiException {

    public ServizioEsternoException(HttpStatus status, String message) {
        super(status, status == HttpStatus.SERVICE_UNAVAILABLE ? "Servizio non disponibile" : "Errore servizio esterno",
                message);
    }
}
