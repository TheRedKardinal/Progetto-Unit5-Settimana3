package com.example.be.controller;

import com.example.be.dto.favorite.FavoriteResponse;
import com.example.be.dto.favorite.SogliaRequest;
import com.example.be.security.CurrentUser;
import com.example.be.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Preferiti dell'utente loggato. */
@RestController
@RequestMapping("/api/me/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<FavoriteResponse> elenco(@AuthenticationPrincipal Jwt jwt) {
        return favoriteService.elenco(CurrentUser.id(jwt));
    }

    @PutMapping("/{carId}")
    public FavoriteResponse aggiungi(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID carId) {
        return favoriteService.aggiungi(CurrentUser.id(jwt), carId);
    }

    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID carId) {
        favoriteService.rimuovi(CurrentUser.id(jwt), carId);
    }

    @PatchMapping("/{carId}/soglia")
    public FavoriteResponse impostaSoglia(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID carId,
                                          @Valid @RequestBody SogliaRequest req) {
        return favoriteService.impostaSoglia(CurrentUser.id(jwt), carId, req.sogliaPrezzo());
    }
}
