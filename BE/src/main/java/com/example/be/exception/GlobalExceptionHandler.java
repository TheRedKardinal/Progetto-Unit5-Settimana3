package com.example.be.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Tutte le altre eccezioni standard di Spring MVC gestite dalla classe padre passano di qui:
     * manteniamo lo status e traduciamo titolo e messaggio in italiano.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        ProblemDetail pd = body instanceof ProblemDetail p ? p : ProblemDetail.forStatus(statusCode);
        pd.setTitle(titoloItaliano(statusCode));
        pd.setDetail(messaggioItaliano(ex, statusCode));
        pd.setProperty("timestamp", Instant.now());
        return super.handleExceptionInternal(ex, pd, headers, statusCode, request);
    }

    private static String messaggioItaliano(Exception ex, HttpStatusCode status) {
        return switch (ex) {
            case TypeMismatchException tm -> messaggioTipoErrato(tm);
            case MissingServletRequestParameterException mp ->
                    "Parametro obbligatorio mancante: '" + mp.getParameterName() + "'";
            case HttpMessageNotReadableException ignored ->
                    "Il corpo della richiesta non è un JSON valido o contiene valori non ammessi";
            case HttpRequestMethodNotSupportedException ms ->
                    "Metodo " + ms.getMethod() + " non supportato per questa risorsa";
            case HttpMediaTypeNotSupportedException ignored ->
                    "Tipo di contenuto non supportato: usa application/json";
            case NoResourceFoundException ignored -> "Risorsa non trovata";
            case HandlerMethodValidationException ignored -> "Uno o più parametri non sono validi";
            default -> status.is5xxServerError() ? "Si è verificato un errore imprevisto" : "Richiesta non valida";
        };
    }

    private static String messaggioTipoErrato(TypeMismatchException ex) {
        String nome = ex.getPropertyName() != null ? ex.getPropertyName() : "parametro";
        Class<?> tipo = ex.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            String ammessi = Arrays.stream(tipo.getEnumConstants()).map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Valore non valido per '" + nome + "': '" + ex.getValue() + "'. Valori ammessi: " + ammessi;
        }
        if (tipo == UUID.class) {
            return "Identificativo non valido: '" + ex.getValue() + "'";
        }
        if (tipo != null && Number.class.isAssignableFrom(tipo)) {
            return "'" + nome + "' deve essere un numero";
        }
        return "Valore non valido per '" + nome + "': '" + ex.getValue() + "'";
    }

    private static String titoloItaliano(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Richiesta non valida";
            case 401 -> "Non autenticato";
            case 403 -> "Accesso negato";
            case 404 -> "Risorsa non trovata";
            case 405 -> "Metodo non consentito";
            case 406 -> "Formato di risposta non disponibile";
            case 409 -> "Conflitto";
            case 413 -> "Richiesta troppo grande";
            case 415 -> "Tipo di contenuto non supportato";
            case 503 -> "Servizio non disponibile";
            default -> status.is5xxServerError() ? "Errore interno" : "Errore";
        };
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
