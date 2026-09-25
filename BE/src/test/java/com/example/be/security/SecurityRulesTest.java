package com.example.be.security;

import com.example.be.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityRulesTest extends IntegrationTestBase {

    @Test
    void catalogoPubblicoAccessibileSenzaLogin() throws Exception {
        mockMvc.perform(get("/api/cars")).andExpect(status().isOk());
    }

    @Test
    void areaAdminSenzaTokenRisponde401ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/admin/cars"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Non autenticato"));
    }

    @Test
    void areaAdminConUtenteNormaleRisponde403() throws Exception {
        var user = creaUtente("mario", true);
        mockMvc.perform(get("/api/admin/cars").header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Accesso negato"));
    }

    @Test
    void areaAdminConAdminRisponde200() throws Exception {
        mockMvc.perform(get("/api/admin/cars").header(HttpHeaders.AUTHORIZATION, bearer(admin())))
                .andExpect(status().isOk());
    }

    @Test
    void meRichiedeIlTokenAncheSeStaSottoApiAuth() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/me/favorites")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFalsoRisponde401() throws Exception {
        mockMvc.perform(get("/api/me/favorites").header(HttpHeaders.AUTHORIZATION, "Bearer a.b.c"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAmmetteSoloIlFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://sito-malevolo.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
