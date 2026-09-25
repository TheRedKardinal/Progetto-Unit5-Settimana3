package com.example.be.client;

import com.example.be.config.AppProperties;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.ServizioEsternoException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test del client contro un finto auto.dev locale (JSON presi dalla documentazione ufficiale):
 * nessuna chiamata reale, la quota gratuita non viene consumata.
 */
class AutoDevClientTest {

    private static final String VIN_OK = """
            {"vin":"3GCUDHEL3NG668790","vinValid":true,"make":"Chevrolet","model":"Silverado 1500","trim":"ZR2",
             "engine":"5.3L V8 OHV 16V FFV","vehicle":{"vin":"3GCUDHEL3NG668790","year":2022,"make":"Chevrolet",
             "model":"Silverado 1500","manufacturer":"General Motors de Mexico"},"ambiguous":false}
            """;
    private static final String FOTO_OK = """
            {"data":{"retail":["https://api.auto.dev/photos/retail/3GCUDHEL3NG668790-1.jpg",
                               "https://api.auto.dev/photos/retail/3GCUDHEL3NG668790-2.jpg"]}}
            """;

    private HttpServer server;
    private final Map<String, Risposta> risposte = new ConcurrentHashMap<>();
    private final Map<String, String> headerAuth = new ConcurrentHashMap<>();
    private final Map<String, String> query = new ConcurrentHashMap<>();
    private AutoDevClient client;

    record Risposta(int status, String body) {
    }

    @BeforeEach
    void avviaStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            headerAuth.put(path, String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
            query.put(path, String.valueOf(exchange.getRequestURI().getRawQuery()));
            Risposta r = risposte.getOrDefault(path, new Risposta(404, "{\"status\":404}"));
            byte[] body = r.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(r.status(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new AutoDevClient(new AppProperties(null, null, null,
                new AppProperties.AutoDev(baseUrl, "chiave-test"), null));
    }

    @AfterEach
    void fermaStub() {
        server.stop(0);
    }

    @Test
    void decodificaVinConLaChiaveNellHeader() {
        risposte.put("/vin/3GCUDHEL3NG668790", new Risposta(200, VIN_OK));

        var dati = client.decodificaVin("3GCUDHEL3NG668790").orElseThrow();

        assertThat(dati.make()).isEqualTo("Chevrolet");
        assertThat(dati.model()).isEqualTo("Silverado 1500");
        assertThat(dati.trim()).isEqualTo("ZR2");
        assertThat(dati.year()).isEqualTo(2022);
        assertThat(headerAuth.get("/vin/3GCUDHEL3NG668790")).isEqualTo("Bearer chiave-test");
    }

    @Test
    void vinSconosciutoRestituisceVuoto() {
        assertThat(client.decodificaVin("3GCUDHEL3NG668790")).isEmpty();
    }

    @Test
    void vinRifiutatoDaAutoDevDiventaBadRequest() {
        risposte.put("/vin/XXX", new Risposta(400, "{\"code\":\"INVALID_VIN_FORMAT\"}"));
        assertThatThrownBy(() -> client.decodificaVin("XXX")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void fotoLeggeDataRetail() {
        risposte.put("/photos/3GCUDHEL3NG668790", new Risposta(200, FOTO_OK));
        assertThat(client.foto("3GCUDHEL3NG668790")).hasSize(2)
                .first().asString().endsWith("-1.jpg");
    }

    @Test
    void fotoAssentiListaVuota() {
        assertThat(client.foto("3GCUDHEL3NG668790")).isEmpty();
        risposte.put("/photos/3GCUDHEL3NG668790", new Risposta(200, "{\"data\":{\"retail\":[]}}"));
        assertThat(client.foto("3GCUDHEL3NG668790")).isEmpty();
    }

    @Test
    void annunciPassaIFiltriELeggeData() {
        risposte.put("/listings", new Risposta(200, """
                {"data":[{"vin":"WP0AF2A99KS165242","vehicle":{"year":2019,"make":"Porsche","model":"911",
                  "trim":"GT3 RS","fuel":"Premium Unleaded"},
                  "retailListing":{"price":189900,"miles":"8120","primaryImage":"https://img.example/1.jpg"}}],
                 "links":{},"total":1}
                """));

        var annunci = client.annunci("Porsche,Aston Martin", "90000-2000000", 2, 20);

        assertThat(annunci).hasSize(1);
        assertThat(annunci.getFirst().vehicle().make()).isEqualTo("Porsche");
        assertThat(annunci.getFirst().retailListing().miles()).isEqualByComparingTo("8120");
        assertThat(query.get("/listings"))
                .contains("vehicle.make=Porsche,Aston%20Martin")
                .contains("retailListing.price=90000-2000000")
                .contains("page=2")
                .contains("limit=20");
    }

    @Test
    void quotaEsauritaDiventa503() {
        risposte.put("/vin/3GCUDHEL3NG668790", new Risposta(429, "{}"));
        assertThatThrownBy(() -> client.decodificaVin("3GCUDHEL3NG668790"))
                .isInstanceOf(ServizioEsternoException.class)
                .hasMessageContaining("Limite di richieste");
    }

    @Test
    void chiaveNonValidaDiventa502() {
        risposte.put("/photos/3GCUDHEL3NG668790", new Risposta(401, "{}"));
        assertThatThrownBy(() -> client.foto("3GCUDHEL3NG668790"))
                .isInstanceOf(ServizioEsternoException.class)
                .hasMessageContaining("Chiave auto.dev non valida");
    }

    @Test
    void senzaChiaveNonChiamaAutoDev() {
        var nonConfigurato = new AutoDevClient(new AppProperties(null, null, null,
                new AppProperties.AutoDev("http://localhost:" + server.getAddress().getPort(), ""), null));
        assertThatThrownBy(() -> nonConfigurato.decodificaVin("3GCUDHEL3NG668790"))
                .isInstanceOf(ServizioEsternoException.class)
                .hasMessageContaining("non configurata");
        assertThat(headerAuth).isEmpty();
    }
}
