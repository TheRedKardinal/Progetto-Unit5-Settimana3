package com.example.be.exception;

import org.springframework.http.HttpStatus;

public class EmailNonVerificataException extends ApiException {

    public EmailNonVerificataException() {
        super(HttpStatus.FORBIDDEN, "Email non verificata",
                "Conferma il tuo indirizzo email tramite il link che ti abbiamo inviato prima di accedere");
    }
}
