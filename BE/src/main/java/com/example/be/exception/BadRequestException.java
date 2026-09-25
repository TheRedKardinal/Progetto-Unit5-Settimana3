package com.example.be.exception;

import org.springframework.http.HttpStatus;

/** Es. token scaduto o già usato, parametro di ordinamento non ammesso. */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "Richiesta non valida", message);
    }
}
