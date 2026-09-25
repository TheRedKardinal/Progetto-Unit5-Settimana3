package com.example.be.controller;

import com.example.be.IntegrationTestBase;
import com.example.be.entity.StatoAnnuncio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogoTest extends IntegrationTestBase {

    @BeforeEach
    void dati() {
        creaAuto("Volkswagen Golf 1.6 TDI", "Volkswagen", "Golf", new BigDecimal("15900.00"), StatoAnnuncio.PUBBLICATO);
        creaAuto("BMW Serie 3 100%_nuova", "BMW", "Serie 3", new BigDecimal("48500.00"), StatoAnnuncio.PUBBLICATO);
        creaAuto("Fiat Panda in bozza", "Fiat", "Panda", new BigDecimal("9000.00"), StatoAnnuncio.BOZZA);
    }

    @Test
    void mostraSoloGliAnnunciPubblicati() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].copertina").exists());
        mockMvc.perform(get("/api/cars").param("q", "panda"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void dettaglioDiUnaBozzaRisponde404() throws Exception {
        var bozza = carRepository.findAll().stream()
                .filter(c -> c.getStatoAnnuncio() == StatoAnnuncio.BOZZA).findFirst().orElseThrow();
        mockMvc.perform(get("/api/cars/{id}", bozza.getId())).andExpect(status().isNotFound());
    }

    @Test
    void ricercaPerParoleCaseInsensitiveEConCaratteriJolly() throws Exception {
        mockMvc.perform(get("/api/cars").param("q", "GOLF tdi"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].marca").value("Volkswagen"));
        // % e _ cercati come caratteri, non come jolly
        mockMvc.perform(get("/api/cars").param("q", "100%"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].marca").value("BMW"));
        mockMvc.perform(get("/api/cars").param("q", "_"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filtriEOrdinamento() throws Exception {
        mockMvc.perform(get("/api/cars").param("sort", "prezzo,asc"))
                .andExpect(jsonPath("$.content[0].marca").value("Volkswagen"))
                .andExpect(jsonPath("$.content[1].marca").value("BMW"));
        mockMvc.perform(get("/api/cars").param("sort", "prezzo,desc"))
                .andExpect(jsonPath("$.content[0].marca").value("BMW"));
        mockMvc.perform(get("/api/cars").param("prezzoMin", "20000"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/cars").param("size", "1").param("page", "1").param("sort", "prezzo,asc"))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].marca").value("BMW"));
    }

    @Test
    void parametriNonValidiRispondono400InItaliano() throws Exception {
        mockMvc.perform(get("/api/cars").param("sort", "descrizione,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Ordinamento non ammesso")));
        mockMvc.perform(get("/api/cars").param("carburante", "vapore"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Valori ammessi")));
        mockMvc.perform(get("/api/cars").param("prezzoMin", "5000").param("prezzoMax", "1000"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/cars/non-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Identificativo non valido")));
    }
}
