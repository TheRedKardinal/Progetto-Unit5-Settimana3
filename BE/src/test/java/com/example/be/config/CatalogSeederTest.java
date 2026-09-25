package com.example.be.config;

import com.example.be.client.AutoDevClient.Listing;
import com.example.be.client.AutoDevClient.ListingVehicle;
import com.example.be.client.AutoDevClient.RetailListing;
import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

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
    void scartaAnnunciSenzaFotoPrezzoOVin() {
        ListingVehicle v = veicolo("Gasoline", null);
        assertThat(CatalogSeeder.daAnnuncio(annuncio(v, "150000", "100", null))).isEmpty();
        assertThat(CatalogSeeder.daAnnuncio(annuncio(v, null, "100", "https://img.example/4.jpg"))).isEmpty();
        assertThat(CatalogSeeder.daAnnuncio(new Listing("CORTO", v,
                new RetailListing(new BigDecimal("150000"), null, "https://img.example/5.jpg")))).isEmpty();
    }
}
