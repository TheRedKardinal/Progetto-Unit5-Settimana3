package com.example.be.dto.car;

import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Creazione e modifica di un annuncio (area admin). Lo stato si cambia con gli endpoint pubblica/bozza. */
public record CarRequest(
        // 17 caratteri, senza I, O, Q (standard ISO 3779). Facoltativo.
        @Pattern(regexp = "^$|^[A-HJ-NPR-Za-hj-npr-z0-9]{17}$",
                message = "Il VIN deve avere 17 caratteri alfanumerici (senza I, O, Q)")
        String vin,

        @NotBlank(message = "La marca è obbligatoria")
        @Size(max = 60, message = "La marca può avere al massimo 60 caratteri")
        String marca,

        @NotBlank(message = "Il modello è obbligatorio")
        @Size(max = 80, message = "Il modello può avere al massimo 80 caratteri")
        String modello,

        @NotNull(message = "L'anno è obbligatorio")
        @Min(value = 1900, message = "Anno non valido")
        @Max(value = 2100, message = "Anno non valido")
        Integer anno,

        @NotBlank(message = "Il titolo è obbligatorio")
        @Size(max = 150, message = "Il titolo può avere al massimo 150 caratteri")
        String titolo,

        @NotBlank(message = "La descrizione è obbligatoria")
        @Size(max = 10000, message = "La descrizione può avere al massimo 10000 caratteri")
        String descrizione,

        @NotNull(message = "Il chilometraggio è obbligatorio")
        @PositiveOrZero(message = "Il chilometraggio non può essere negativo")
        @Digits(integer = 9, fraction = 1, message = "Chilometraggio non valido")
        BigDecimal chilometraggio,

        @NotNull(message = "Il carburante è obbligatorio")
        Carburante carburante,

        @NotNull(message = "Il prezzo è obbligatorio")
        @Positive(message = "Il prezzo deve essere maggiore di zero")
        @Digits(integer = 10, fraction = 2, message = "Prezzo non valido (max 2 decimali)")
        BigDecimal prezzo,

        @NotNull(message = "La condizione è obbligatoria")
        Condizione condizione,

        @Size(max = 20, message = "Puoi inserire al massimo 20 immagini")
        List<
                @NotBlank(message = "URL immagine vuoto")
                @Size(max = 1000, message = "URL immagine troppo lungo")
                @Pattern(regexp = "^https?://\\S+$", message = "URL immagine non valido (deve iniziare con http:// o https://)")
                        String> immagini
) {
}
