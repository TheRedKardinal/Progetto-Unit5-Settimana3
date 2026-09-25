package com.example.be.exception;

import org.springframework.http.HttpStatus;

/** Es. email o username già registrati. */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "Conflitto", message);
    }
}
