package com.example.be.controller;

import com.example.be.dto.PageResponse;
import com.example.be.dto.car.CarFilter;
import com.example.be.dto.car.CarResponse;
import com.example.be.dto.car.CarSummaryResponse;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/** Catalogo pubblico (accessibile anche senza login). */
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    /**
     * Esempio: /api/cars?q=golf&carburante=DIESEL&prezzoMax=20000&page=0&size=12&sort=prezzo,asc
     * Ordinamenti ammessi: prezzo, chilometraggio, anno, publishedAt (default: publishedAt desc).
     */
    @GetMapping
    public PageResponse<CarSummaryResponse> cerca(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Carburante carburante,
            @RequestParam(required = false) Condizione condizione,
            @RequestParam(required = false) BigDecimal prezzoMin,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @PageableDefault(size = 12, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        CarFilter filtro = new CarFilter(q, carburante, condizione, prezzoMin, prezzoMax, null);
        return carService.cercaPubblicati(filtro, pageable);
    }

    @GetMapping("/{id}")
    public CarResponse dettaglio(@PathVariable UUID id) {
        return carService.dettaglioPubblicato(id);
    }
}
