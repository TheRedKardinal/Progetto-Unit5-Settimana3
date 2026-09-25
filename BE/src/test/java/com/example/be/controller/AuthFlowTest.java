package com.example.be.controller;

import com.example.be.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends IntegrationTestBase {

    private static final String REGISTRAZIONE = """
            {"nome":"Anna","cognome":"Bianchi","username":"anna","email":"Anna@Example.com","password":"password123"}
            """;

    @Test
    void registrazioneVerificaEmailELogin() throws Exception {
        postJson("/api/auth/register", REGISTRAZIONE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("anna@example.com"))
                .andExpect(jsonPath("$.ruoli[0]").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists());

        // Prima della verifica il login è bloccato
        postJson("/api/auth/login", """
                {"emailOrUsername":"anna","password":"password123"}""")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Email non verificata"));

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService, timeout(2000)).inviaVerificaEmail(eq("anna@example.com"), eq("Anna"), token.capture());

        postJson("/api/auth/verify-email", "{\"token\":\"" + token.getValue() + "\"}").andExpect(status().isOk());
        // Monouso
        postJson("/api/auth/verify-email", "{\"token\":\"" + token.getValue() + "\"}").andExpect(status().isBadRequest());

        // Login sia con email (case-insensitive) sia con username
        postJson("/api/auth/login", """
                {"emailOrUsername":"ANNA@example.com","password":"password123"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        postJson("/api/auth/login", """
                {"emailOrUsername":"Anna","password":"password123"}""")
                .andExpect(status().isOk());
    }

    @Test
    void emailEUsernameDuplicatiRispondono409() throws Exception {
        postJson("/api/auth/register", REGISTRAZIONE).andExpect(status().isCreated());

        postJson("/api/auth/register", """
                {"nome":"A","cognome":"B","username":"altro","email":"anna@example.com","password":"password123"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email già registrata"));
        postJson("/api/auth/register", """
                {"nome":"A","cognome":"B","username":"ANNA","email":"altra@example.com","password":"password123"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username già in uso"));
    }

    @Test
    void validazioneRegistrazione() throws Exception {
        postJson("/api/auth/register", """
                {"nome":"","email":"non-una-email","password":"corta"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.email").value("Email non valida"))
                .andExpect(jsonPath("$.errors.password").value("La password deve avere tra 8 e 64 caratteri"));

        // 40 "è" = 80 byte: oltre il limite di BCrypt → 400 e non 500
        postJson("/api/auth/register", """
                {"nome":"A","cognome":"B","username":"byte","email":"byte@example.com","password":"%s"}"""
                .formatted("è".repeat(40)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void credenzialiErrateStessoMessaggioPerUtenteInesistente() throws Exception {
        creaUtente("luca", true);
        postJson("/api/auth/login", """
                {"emailOrUsername":"luca","password":"sbagliata"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email/username o password errati"));
        postJson("/api/auth/login", """
                {"emailOrUsername":"nessuno","password":"sbagliata"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email/username o password errati"));
    }

    @Test
    void passwordDimenticataEReset() throws Exception {
        creaUtente("giulia", false);

        // Stessa risposta per email esistente e inesistente
        String messaggio = "Se l'indirizzo è registrato, riceverai un'email con il link per reimpostare la password";
        postJson("/api/auth/forgot-password", "{\"email\":\"nessuno@example.com\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value(messaggio));
        postJson("/api/auth/forgot-password", "{\"email\":\"giulia@example.com\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value(messaggio));

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService, timeout(2000))
                .inviaResetPassword(eq("giulia@example.com"), anyString(), token.capture(), eq(15L));
        verify(emailService, never()).inviaResetPassword(eq("nessuno@example.com"), anyString(), anyString(), anyLong());

        postJson("/api/auth/reset-password",
                "{\"token\":\"" + token.getValue() + "\",\"nuovaPassword\":\"nuovaPassword1\"}")
                .andExpect(status().isOk());
        postJson("/api/auth/reset-password",
                "{\"token\":\"" + token.getValue() + "\",\"nuovaPassword\":\"altraPassword1\"}")
                .andExpect(status().isBadRequest());

        postJson("/api/auth/login", """
                {"emailOrUsername":"giulia","password":"password123"}""")
                .andExpect(status().isUnauthorized());
        // Il reset conferma anche l'email
        postJson("/api/auth/login", """
                {"emailOrUsername":"giulia","password":"nuovaPassword1"}""")
                .andExpect(status().isOk());
        assertThat(userRepository.findByUsernameIgnoreCase("giulia").orElseThrow().isEmailVerificata()).isTrue();
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
