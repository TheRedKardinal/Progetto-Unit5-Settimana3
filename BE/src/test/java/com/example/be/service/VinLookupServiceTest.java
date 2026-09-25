package com.example.be.service;

import com.example.be.client.AutoDevClient;
import com.example.be.config.CacheConfig;
import com.example.be.entity.Carburante;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Contesto minimo: solo il service, la cache e un AutoDevClient finto. */
@SpringJUnitConfig({VinLookupService.class, CacheConfig.class})
class VinLookupServiceTest {

    private static final String VIN = "WP0AF2A99KS165242";

    @Autowired
    private VinLookupService service;

    @MockitoBean
    private AutoDevClient autoDevClient;

    @Test
    void combinaDatiEFotoEMetteInCache() {
        when(autoDevClient.decodificaVin(VIN)).thenReturn(Optional.of(new AutoDevClient.VinDecode(
                VIN, "Porsche", "911", "GT3", "4.0L H6 DOHC 24V", new AutoDevClient.Vehicle(2019, "Porsche", "911"))));
        when(autoDevClient.foto(VIN)).thenReturn(List.of("https://example.com/1.jpg"));

        var risultato = service.cerca(VIN);
        service.cerca(VIN);

        assertThat(risultato.marca()).isEqualTo("Porsche");
        assertThat(risultato.modello()).isEqualTo("911 GT3");
        assertThat(risultato.anno()).isEqualTo(2019);
        assertThat(risultato.immagini()).containsExactly("https://example.com/1.jpg");
        // Seconda ricerca servita dalla cache: nessuna chiamata in più alla quota di auto.dev
        verify(autoDevClient, times(1)).decodificaVin(VIN);
        verify(autoDevClient, times(1)).foto(VIN);
    }

    @Test
    void vinSconosciutoNonChiedeLeFotoENonVaInCache() {
        String sconosciuto = "1HGCM82633A004352";
        when(autoDevClient.decodificaVin(sconosciuto)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cerca(sconosciuto)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.cerca(sconosciuto)).isInstanceOf(NotFoundException.class);

        verify(autoDevClient, never()).foto(anyString());
        verify(autoDevClient, times(2)).decodificaVin(sconosciuto);
    }

    @Test
    void normalizzazioneEValidazioneVin() {
        assertThat(VinLookupService.normalizza("  wp0af2a99ks165242 ")).isEqualTo(VIN);
        assertThatThrownBy(() -> VinLookupService.normalizza("ABC")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> VinLookupService.normalizza("WP0AF2A99KS16524O")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void deduzioneCarburanteDalMotore() {
        assertThat(VinLookupService.deduciCarburante("2.0L I4 TDI Diesel")).isEqualTo(Carburante.DIESEL);
        assertThat(VinLookupService.deduciCarburante("Electric Motor")).isEqualTo(Carburante.ELETTRICA);
        assertThat(VinLookupService.deduciCarburante("2.5L I4 Hybrid")).isEqualTo(Carburante.IBRIDA);
        assertThat(VinLookupService.deduciCarburante("1.4L I4 Gas")).isEqualTo(Carburante.BENZINA);
        // Non deducibile con certezza: lo sceglie l'admin
        assertThat(VinLookupService.deduciCarburante("5.3L V8 OHV 16V FFV")).isNull();
        assertThat(VinLookupService.deduciCarburante(null)).isNull();
    }
}
