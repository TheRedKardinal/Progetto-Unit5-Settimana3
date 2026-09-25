package com.example.be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Eccezione applicativa con lo status HTTP da restituire.
 * Il messaggio finisce nel campo "detail" del ProblemDetail, quindi deve essere leggibile dall'utente.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;

    protected ApiException(HttpStatus status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }
}
