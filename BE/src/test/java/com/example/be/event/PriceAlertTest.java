package com.example.be.event;

import com.example.be.IntegrationTestBase;
import com.example.be.entity.Car;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Soglia di prezzo sui preferiti: il listener è asincrono, quindi le verifiche usano timeout/after. */
class PriceAlertTest extends IntegrationTestBase {

    private User utente;
    private Car auto;

    @BeforeEach
    void preparaPreferitoConSoglia() throws Exception {
        utente = creaUtente("luca", true);
        auto = creaAuto("Fiat 500", "Fiat", "500", new BigDecimal("15000.00"), StatoAnnuncio.PUBBLICATO);

        mockMvc.perform(put("/api/me/favorites/{id}", auto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(utente)))
                .andExpect(status().isOk());
        impostaSoglia("14000");
    }

    @Test
    void notificaQuandoIlPrezzoScendeSottoLaSoglia() throws Exception {
        cambiaPrezzo("14500");
        verify(emailService, after(500).never()).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());

        cambiaPrezzo("13900");
        verify(emailService, timeout(3000)).inviaNotificaPrezzo(eq("luca@example.com"), anyString(), eq(auto.getId()),
                eq("Fiat 500"), eq(new BigDecimal("13900.00")), eq(new BigDecimal("14000.00")));
    }

    @Test
    void nessunaEmailDoppiaFinchéIlPrezzoNonRisale() throws Exception {
        cambiaPrezzo("13900");
        cambiaPrezzo("13500");
        verify(emailService, after(1000).times(1)).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());

        // Risale sopra la soglia e riscende: nuova notifica
        cambiaPrezzo("14200");
        cambiaPrezzo("13800");
        verify(emailService, timeout(3000).times(2)).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());
    }

    @Test
    void sogliaGiàSuperataNotificaSubito() throws Exception {
        impostaSoglia("20000");
        verify(emailService, timeout(3000)).inviaNotificaPrezzo(eq("luca@example.com"), anyString(), eq(auto.getId()),
                anyString(), eq(new BigDecimal("15000.00")), eq(new BigDecimal("20000.00")));
    }

    @Test
    void annuncioInBozzaNonNotifica() throws Exception {
        mockMvc.perform(post("/api/admin/cars/{id}/bozza", auto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(admin())))
                .andExpect(status().isOk());
        cambiaPrezzo("10000");
        verify(emailService, after(1000).never()).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());

        // Alla pubblicazione le soglie vengono ricontrollate
        mockMvc.perform(post("/api/admin/cars/{id}/pubblica", auto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(admin())))
                .andExpect(status().isOk());
        verify(emailService, timeout(3000)).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());
    }

    @Test
    void sogliaRimossaNonNotifica() throws Exception {
        mockMvc.perform(patch("/api/me/favorites/{id}/soglia", auto.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(utente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sogliaPrezzo\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sogliaPrezzo").doesNotExist());
        cambiaPrezzo("5000");
        verify(emailService, after(1000).never()).inviaNotificaPrezzo(anyString(), anyString(), any(), anyString(), any(), any());
    }

    private void impostaSoglia(String soglia) throws Exception {
        mockMvc.perform(patch("/api/me/favorites/{id}/soglia", auto.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(utente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sogliaPrezzo\":" + soglia + "}"))
                .andExpect(status().isOk());
    }

    private void cambiaPrezzo(String prezzo) throws Exception {
        mockMvc.perform(patch("/api/admin/cars/{id}/prezzo", auto.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prezzo\":" + prezzo + "}"))
                .andExpect(status().isOk());
        // Lascia al listener asincrono il tempo di processare l'evento prima del cambio successivo
        Thread.sleep(300);
    }
}
