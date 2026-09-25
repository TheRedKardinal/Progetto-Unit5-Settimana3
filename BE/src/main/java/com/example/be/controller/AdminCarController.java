package com.example.be.controller;

import com.example.be.dto.PageResponse;
import com.example.be.dto.car.CarFilter;
import com.example.be.dto.car.CarRequest;
import com.example.be.dto.car.CarResponse;
import com.example.be.dto.car.CarSummaryResponse;
import com.example.be.dto.car.PrezzoRequest;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/** Gestione annunci: accesso riservato al ruolo ADMIN (vedi SecurityConfig). */
@RestController
@RequestMapping("/api/admin/cars")
@RequiredArgsConstructor
public class AdminCarController {

    private final CarService carService;

    /** Come il catalogo pubblico, più il filtro per stato e le bozze. Default: ultimi modificati. */
    @GetMapping
    public PageResponse<CarSummaryResponse> elenco(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatoAnnuncio stato,
            @RequestParam(required = false) Carburante carburante,
            @RequestParam(required = false) Condizione condizione,
            @RequestParam(required = false) BigDecimal prezzoMin,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        CarFilter filtro = new CarFilter(q, carburante, condizione, prezzoMin, prezzoMax, stato);
        return carService.cercaAdmin(filtro, pageable);
    }

    @GetMapping("/{id}")
    public CarResponse dettaglio(@PathVariable UUID id) {
        return carService.dettaglioAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse crea(@Valid @RequestBody CarRequest req) {
        return carService.crea(req);
    }

    @PutMapping("/{id}")
    public CarResponse aggiorna(@PathVariable UUID id, @Valid @RequestBody CarRequest req) {
        return carService.aggiorna(id, req);
    }

    @PatchMapping("/{id}/prezzo")
    public CarResponse aggiornaPrezzo(@PathVariable UUID id, @Valid @RequestBody PrezzoRequest req) {
        return carService.aggiornaPrezzo(id, req.prezzo());
    }

    @PostMapping("/{id}/pubblica")
    public CarResponse pubblica(@PathVariable UUID id) {
        return carService.pubblica(id);
    }

    @PostMapping("/{id}/bozza")
    public CarResponse bozza(@PathVariable UUID id) {
        return carService.bozza(id);
    }
}
