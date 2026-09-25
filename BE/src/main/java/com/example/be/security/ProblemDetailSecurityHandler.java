package com.example.be.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 401 e 403 generati dai filtri di Spring Security (prima dei controller, quindi fuori dal
 * GlobalExceptionHandler) restituiti comunque come ProblemDetail.
 */
@Component
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        scrivi(response, HttpStatus.UNAUTHORIZED, "Non autenticato",
                "Effettua il login per accedere a questa risorsa", request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        scrivi(response, HttpStatus.FORBIDDEN, "Accesso negato",
                "Non hai i permessi per accedere a questa risorsa", request.getRequestURI());
    }

    private static void scrivi(HttpServletResponse response, HttpStatus status, String title,
                               String detail, String instance) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Testi fissi senza caratteri da escapare, tranne l'URI che viene ripulito
        String json = """
                {"type":"about:blank","title":"%s","status":%d,"detail":"%s","instance":"%s","timestamp":"%s"}"""
                .formatted(title, status.value(), detail, instance.replace("\"", "").replace("\\", ""), Instant.now());
        response.getWriter().write(json);
    }
}
