package com.example.be.client;

import com.example.be.config.AppProperties;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.ServizioEsternoException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Client per le API di auto.dev (piano gratuito, quota limitata).
 * La chiave resta lato server: il frontend passa sempre da /api/admin/vin/{vin}.
 */
@Slf4j
@Component
public class AutoDevClient {

    private final RestClient restClient;
    private final boolean configurato;

    public AutoDevClient(AppProperties props) {
        AppProperties.AutoDev conf = props.autodev();
        this.configurato = conf.apiKey() != null && !conf.apiKey().isBlank();

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(conf.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + conf.apiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** GET /vin/{vin}. Vuoto se auto.dev non conosce il VIN (404). */
    public Optional<VinDecode> decodificaVin(String vin) {
        verificaConfigurazione();
        try {
            return Optional.ofNullable(restClient.get().uri("/vin/{vin}", vin).retrieve().body(VinDecode.class));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            if (e.getStatusCode().value() == 400) {
                throw new BadRequestException("VIN non valido: deve avere 17 caratteri alfanumerici (senza I, O, Q)");
            }
            throw traduci(e);
        } catch (ResourceAccessException e) {
            throw nonRaggiungibile(e);
        }
    }

    /** GET /photos/{vin} → data.retail[]. Lista vuota se non ci sono foto (404 o lista vuota). */
    public List<String> foto(String vin) {
        verificaConfigurazione();
        try {
            PhotosResponse res = restClient.get().uri("/photos/{vin}", vin).retrieve().body(PhotosResponse.class);
            if (res == null || res.data() == null || res.data().retail() == null) {
                return List.of();
            }
            return res.data().retail();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return List.of();
            }
            throw traduci(e);
        } catch (ResourceAccessException e) {
            throw nonRaggiungibile(e);
        }
    }

    private void verificaConfigurazione() {
        if (!configurato) {
            throw new ServizioEsternoException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Integrazione auto.dev non configurata (AUTODEV_API_KEY mancante)");
        }
    }

    private static ServizioEsternoException traduci(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        // Il body può contenere dettagli utili al debug; la chiave non compare mai nelle risposte di auto.dev
        log.warn("auto.dev ha risposto {}: {}", status, e.getResponseBodyAsString());
        if (status == 429) {
            return new ServizioEsternoException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Limite di richieste ad auto.dev raggiunto: riprova più tardi o inserisci i dati a mano");
        }
        if (status == 401 || status == 403) {
            return new ServizioEsternoException(HttpStatus.BAD_GATEWAY,
                    "Chiave auto.dev non valida o piano non abilitato");
        }
        return new ServizioEsternoException(HttpStatus.BAD_GATEWAY,
                "auto.dev non ha risposto correttamente (" + status + ")");
    }

    private static ServizioEsternoException nonRaggiungibile(ResourceAccessException e) {
        log.warn("auto.dev non raggiungibile: {}", e.getMessage());
        return new ServizioEsternoException(HttpStatus.BAD_GATEWAY, "auto.dev non raggiungibile");
    }

    // --- Modelli della risposta (solo i campi usati) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VinDecode(String vin, String make, String model, String trim, String engine, Vehicle vehicle) {

        public Integer year() {
            return vehicle == null ? null : vehicle.year();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vehicle(Integer year, String make, String model) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotosResponse(PhotosData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotosData(List<String> retail) {
    }
}
