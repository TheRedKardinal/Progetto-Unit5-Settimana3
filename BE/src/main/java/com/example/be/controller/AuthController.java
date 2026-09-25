package com.example.be.controller;

import com.example.be.dto.MessageResponse;
import com.example.be.dto.auth.AuthResponse;
import com.example.be.dto.auth.EmailRequest;
import com.example.be.dto.auth.LoginRequest;
import com.example.be.dto.auth.RegisterRequest;
import com.example.be.dto.auth.TokenRequest;
import com.example.be.dto.auth.UserResponse;
import com.example.be.security.CurrentUser;
import com.example.be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.registra(req);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody TokenRequest req) {
        authService.verificaEmail(req.token());
        return new MessageResponse("Email verificata: ora puoi effettuare il login");
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody EmailRequest req) {
        authService.reinviaVerifica(req.email());
        return new MessageResponse(
                "Se l'indirizzo è registrato e non ancora verificato, riceverai una nuova email di conferma");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(CurrentUser.id(jwt));
    }
}
