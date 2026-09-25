package com.example.be.controller;

import com.example.be.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCarTest extends IntegrationTestBase {

    private static final String ANNUNCIO = """
            {"vin":"wp0af2a99ks165242","marca":" Porsche ","modello":"911","anno":2019,
             "titolo":"Porsche 911 GT3","descrizione":"Test","chilometraggio":12000,"carburante":"BENZINA",
             "prezzo":150000,"condizione":"USATO",
             "immagini":["https://example.com/a.jpg","https://example.com/b.jpg","https://example.com/a.jpg"]}
            """;

    @Test
    void cicloDiVitaAnnuncio() throws Exception {
        String auth = bearer(admin());

        String json = mockMvc.perform(post("/api/admin/cars").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(ANNUNCIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statoAnnuncio").value("BOZZA"))
                .andExpect(jsonPath("$.vin").value("WP0AF2A99KS165242"))
                .andExpect(jsonPath("$.marca").value("Porsche"))
                .andExpect(jsonPath("$.prezzo").value(150000.00))
                .andExpect(jsonPath("$.immagini.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String id = json.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        // Bozza: invisibile al pubblico, visibile all'admin
        mockMvc.perform(get("/api/cars/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/cars").param("stato", "BOZZA").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post("/api/admin/cars/{id}/pubblica", id).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.statoAnnuncio").value("PUBBLICATO"))
                .andExpect(jsonPath("$.publishedAt").exists());
        mockMvc.perform(get("/api/cars/{id}", id)).andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/cars/{id}", id).header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ANNUNCIO.replace("Porsche 911 GT3", "Porsche 911 GT3 RS")))
                .andExpect(jsonPath("$.titolo").value("Porsche 911 GT3 RS"))
                .andExpect(jsonPath("$.statoAnnuncio").value("PUBBLICATO"));

        mockMvc.perform(post("/api/admin/cars/{id}/bozza", id).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.statoAnnuncio").value("BOZZA"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist());
        mockMvc.perform(get("/api/cars/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void validazioneAnnuncio() throws Exception {
        mockMvc.perform(post("/api/admin/cars").header(HttpHeaders.AUTHORIZATION, bearer(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vin":"corto","anno":1800,"prezzo":-5,"chilometraggio":-1,"immagini":["ftp://x"]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vin").exists())
                .andExpect(jsonPath("$.errors.prezzo").exists())
                .andExpect(jsonPath("$.errors.marca").exists())
                .andExpect(jsonPath("$.errors['immagini[0]']").exists());
    }

    @Test
    void annuncioInesistenteRisponde404() throws Exception {
        mockMvc.perform(post("/api/admin/cars/{id}/pubblica", "00000000-0000-0000-0000-000000000000")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin())))
                .andExpect(status().isNotFound());
    }
}
