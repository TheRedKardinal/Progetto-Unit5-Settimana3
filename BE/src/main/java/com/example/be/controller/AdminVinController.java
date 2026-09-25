package com.example.be.controller;

import com.example.be.dto.car.VinLookupResponse;
import com.example.be.service.VinLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Precompilazione del form annuncio dal VIN (solo ADMIN). */
@RestController
@RequestMapping("/api/admin/vin")
@RequiredArgsConstructor
public class AdminVinController {

    private final VinLookupService vinLookupService;

    @GetMapping("/{vin}")
    public VinLookupResponse cerca(@PathVariable String vin) {
        // Normalizzazione e validazione prima della cache: "wp0..." e "WP0..." sono la stessa chiave,
        // e un VIN malformato non consuma chiamate ad auto.dev
        return vinLookupService.cerca(VinLookupService.normalizza(vin));
    }
}
