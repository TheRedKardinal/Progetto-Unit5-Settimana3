package com.example.be.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tutti gli errori delle API escono come ProblemDetail (RFC 9457, application/problem+json).
 * Estende ResponseEntityExceptionHandler per gestire anche le eccezioni standard di Spring MVC
 * (body JSON illeggibile, parametro con tipo errato, metodo non supportato, ...).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return problem(ex.getStatus(), ex.getTitle(), ex.getMessage());
    }

    /** 400: errori di Bean Validation sul body, con il dettaglio per campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errori = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errori.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> errori.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Dati non validi", "Controlla i campi evidenziati");
        body.setProperty("errors", errori);
        return ResponseEntity.badRequest().body(body);
    }

    /** 409: violazione di un vincolo unique arrivata al DB (es. due registrazioni simultanee). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violazione di integrità dei dati: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflitto", "La risorsa esiste già o viola un vincolo");
    }

    /** 500: qualsiasi altro errore, senza esporre dettagli interni. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Errore non gestito", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno", "Si è verificato un errore imprevisto");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
