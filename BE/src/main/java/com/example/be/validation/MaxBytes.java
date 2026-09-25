package com.example.be.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lunghezza massima in byte UTF-8 (non in caratteri).
 * Serve per le password: BCrypt rifiuta input oltre 72 byte, e 64 caratteri accentati o emoji possono superarli.
 */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {

    int value();

    String message() default "Valore troppo lungo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
