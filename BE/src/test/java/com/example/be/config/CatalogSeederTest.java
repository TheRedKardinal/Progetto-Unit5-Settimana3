package com.example.be.config;

import com.example.be.client.AutoDevClient;
import com.example.be.client.AutoDevClient.Listing;
import com.example.be.client.AutoDevClient.ListingVehicle;
import com.example.be.client.AutoDevClient.RetailListing;
import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.exception.ServizioEsternoException;
import com.example.be.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Conversione degli annunci auto.dev in auto del catalogo (nessuna chiamata reale). */
class CatalogSeederTest {

    private static ListingVehicle veicolo(String fuel, String engine) {
        return new ListingVehicle(2021, "Porsche", "911", "Turbo S", fuel, engine,
                "Automatic", "AWD", "GT Silver Metallic", "Black");
    }

    private static Listing annuncio(ListingVehicle v, String prezzo, String miglia, String foto) {
        return new Listing("wp0ab2a93ms220001", v,
                new RetailListing(prezzo == null ? null : new BigDecimal(prezzo),
                        miglia == null ? null : new BigDecimal(miglia), foto));
    }

    @Test
    void convertePrezzoChilometriECampiPrincipali() {
        Car car = CatalogSeeder.daAnnuncio(annuncio(veicolo("Premium Unleaded", "3.7L H6"),
                "214995", "10000", "https://img.example/1.jpg")).orElseThrow();

        assertThat(car.getVin()).isEqualTo("WP0AB2A93MS220001");
        assertThat(car.getMarca()).isEqualTo("Porsche");
        assertThat(car.getAnno()).isEqualTo(Year.of(2021));
        assertThat(car.getTitolo()).isEqualTo("Porsche 911 Turbo S");
        assertThat(car.getPrezzo()).isEqualByComparingTo("215000");
        assertThat(car.getChilometraggio()).isEqualByComparingTo("16093");
        assertThat(car.getCondizione()).isEqualTo(Condizione.USATO);
        assertThat(car.getCarburante()).isEqualTo(Carburante.BENZINA);
        assertThat(car.getStatoAnnuncio()).isEqualTo(StatoAnnuncio.PUBBLICATO);
        assertThat(car.getPublishedAt()).isNotNull();
        assertThat(car.getImmagini()).containsExactly("https://img.example/1.jpg");
        assertThat(car.getDescrizione()).contains("16.093 km").contains("Motore: 3.7L H6.");
    }

    @Test
    void carburanteECondizioneDedotti() {
        Car ibrida = CatalogSeeder.daAnnuncio(annuncio(veicolo("Hybrid", null), "150000", "5", "https://img.example/2.jpg"))
                .orElseThrow();
        assertThat(ibrida.getCarburante()).isEqualTo(Carburante.IBRIDA);
        assertThat(ibrida.getCondizione()).isEqualTo(Condizione.NUOVO);

        Car kmZero = CatalogSeeder.daAnnuncio(annuncio(veicolo(null, null), "150000", "300", "https://img.example/3.jpg"))
                .orElseThrow();
        assertThat(kmZero.getCondizione()).isEqualTo(Condizione.KM_0);
        assertThat(kmZero.getCarburante()).isEqualTo(Carburante.BENZINA);
    }

    @Test
    void ripulisceIlTrimDeiConcessionari() {
        assertThat(CatalogSeeder.allestimento(trim("488 Pista", "488 Pista *TAILOR MADE * FRONT LIFT*"))).isNull();
        assertThat(CatalogSeeder.allestimento(trim("Huracan STO", "STO"))).isNull();
        assertThat(CatalogSeeder.allestimento(trim("812 GTS", "Unspecified"))).isNull();
        assertThat(CatalogSeeder.allestimento(trim("911", "GT3 RS *ONE OWNER*"))).isEqualTo("GT3 RS");
        assertThat(CatalogSeeder.allestimento(trim("Aventador", "Aventador SVJ Roadster"))).isEqualTo("SVJ Roadster");
    }

    private static ListingVehicle trim(String modello, String trim) {
        return new ListingVehicle(2020, "Marca", modello, trim, null, null, null, null, null, null);
    }

    /** 20 annunci validi della marca, con VIN distinti. */
    private static List<Listing> pagina(String marca, int pagina) {
        return IntStream.range(0, 20).mapToObj(i -> new Listing(
                String.format("%-3.3s%02d%012d", marca.toUpperCase(), pagina, i).replace(' ', 'X'),
                new ListingVehicle(2022, marca, "Modello", null, null, null, null, null, null, null),
                new RetailListing(new BigDecimal("150000"), new BigDecimal("5000"), "https://img.example/" + i + ".jpg")))
                .toList();
    }

    private static CatalogSeeder seeder(AutoDevClient client, CarRepository repo) {
        return new CatalogSeeder(new CatalogSeedProperties(true, 50, "Ferrari | Bentley | Porsche | BMW", "80000-600000"),
                client, repo);
    }

    @Test
    @SuppressWarnings("unchecked")
    void ogniGruppoDiMarcheHaLaSuaQuotaConUnaChiamataCiascuno() {
        AutoDevClient client = mock(AutoDevClient.class);
        CarRepository repo = mock(CarRepository.class);
        when(client.annunci(anyString(), anyString(), anyInt(), anyInt()))
                .thenAnswer(inv -> pagina(inv.getArgument(0), inv.getArgument(2)));

        assertThat(seeder(client, repo).popola(50)).isEqualTo(50);

        ArgumentCaptor<List<Car>> salvate = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(salvate.capture());
        Map<String, Long> perMarca = salvate.getValue().stream()
                .collect(Collectors.groupingBy(Car::getMarca, Collectors.counting()));
        assertThat(perMarca).containsEntry("Ferrari", 13L).containsEntry("Bentley", 13L)
                .containsEntry("Porsche", 13L).containsEntry("BMW", 11L);
        verify(client, times(4)).annunci(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void seAutoDevCadeAMetaSalvaQuelloCheHa() {
        AutoDevClient client = mock(AutoDevClient.class);
        CarRepository repo = mock(CarRepository.class);
        when(client.annunci(anyString(), anyString(), anyInt(), anyInt()))
                .thenAnswer(inv -> {
                    if ("Porsche".equals(inv.getArgument(0))) {
                        throw new ServizioEsternoException(HttpStatus.SERVICE_UNAVAILABLE, "quota esaurita");
                    }
                    return pagina(inv.getArgument(0), inv.getArgument(2));
                });

        assertThat(seeder(client, repo).popola(50)).isEqualTo(26);

        ArgumentCaptor<List<Car>> salvate = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(salvate.capture());
        assertThat(salvate.getValue()).hasSize(26);
    }

    @Test
    void scartaAnnunciSenzaFotoPrezzoOVin() {
        ListingVehicle v = veicolo("Gasoline", null);
        assertThat(CatalogSeeder.daAnnuncio(annuncio(v, "150000", "100", null))).isEmpty();
        assertThat(CatalogSeeder.daAnnuncio(annuncio(v, null, "100", "https://img.example/4.jpg"))).isEmpty();
        assertThat(CatalogSeeder.daAnnuncio(new Listing("CORTO", v,
                new RetailListing(new BigDecimal("150000"), null, "https://img.example/5.jpg")))).isEmpty();
    }
}
