package com.example.be.service;

import com.example.be.dto.PageResponse;
import com.example.be.dto.car.CarFilter;
import com.example.be.dto.car.CarResponse;
import com.example.be.dto.car.CarSummaryResponse;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.NotFoundException;
import com.example.be.repository.CarRepository;
import com.example.be.repository.CarSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    static final Set<String> ORDINAMENTI_PUBBLICI = Set.of("prezzo", "chilometraggio", "anno", "publishedAt");

    private final CarRepository carRepository;

    /** Catalogo pubblico: solo annunci PUBBLICATO, qualunque stato arrivi nel filtro. */
    @Transactional(readOnly = true)
    public PageResponse<CarSummaryResponse> cercaPubblicati(CarFilter filtro, Pageable pageable) {
        validaPrezzi(filtro);
        CarFilter soloPubblicati = new CarFilter(filtro.q(), filtro.carburante(), filtro.condizione(),
                filtro.prezzoMin(), filtro.prezzoMax(), StatoAnnuncio.PUBBLICATO);

        Pageable pagina = Ordinamento.valida(pageable, ORDINAMENTI_PUBBLICI);
        return PageResponse.from(
                carRepository.findAll(CarSpecifications.da(soloPubblicati), pagina).map(CarSummaryResponse::from));
    }

    /** Dettaglio pubblico: un annuncio in bozza risulta inesistente. */
    @Transactional(readOnly = true)
    public CarResponse dettaglioPubblicato(UUID id) {
        return carRepository.findByIdAndStatoAnnuncio(id, StatoAnnuncio.PUBBLICATO)
                .map(CarResponse::from)
                .orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
    }

    static void validaPrezzi(CarFilter f) {
        if (f.prezzoMin() != null && f.prezzoMin().signum() < 0
                || f.prezzoMax() != null && f.prezzoMax().signum() < 0) {
            throw new BadRequestException("I prezzi non possono essere negativi");
        }
        BigDecimal min = f.prezzoMin();
        BigDecimal max = f.prezzoMax();
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("Il prezzo minimo non può superare il prezzo massimo");
        }
    }
}
