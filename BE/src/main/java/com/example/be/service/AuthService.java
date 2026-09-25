package com.example.be.service;

import com.example.be.dto.auth.AuthResponse;
import com.example.be.dto.auth.LoginRequest;
import com.example.be.dto.auth.RegisterRequest;
import com.example.be.dto.auth.UserResponse;
import com.example.be.entity.EmailVerificationToken;
import com.example.be.entity.Role;
import com.example.be.entity.User;
import com.example.be.exception.BadRequestException;
import com.example.be.exception.ConflictException;
import com.example.be.exception.EmailNonVerificataException;
import com.example.be.exception.InvalidCredentialsException;
import com.example.be.exception.NotFoundException;
import com.example.be.repository.EmailVerificationTokenRepository;
import com.example.be.repository.RoleRepository;
import com.example.be.repository.UserRepository;
import com.example.be.security.JwtService;
import com.example.be.security.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    static final Duration VALIDITA_VERIFICA_EMAIL = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    /** Hash calcolato una volta: usato quando l'utente non esiste, per non rivelarlo dai tempi di risposta. */
    private String hashFittizio;

    @Transactional
    public UserResponse registra(RegisterRequest req) {
        String email = User.normalizzaEmail(req.email());
        String username = req.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email già registrata");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username già in uso");
        }

        Role ruoloUser = roleRepository.findByRole(Role.USER)
                .orElseThrow(() -> new IllegalStateException("Ruolo USER mancante: il seeder non è stato eseguito"));

        User user = new User();
        user.setNome(req.nome().trim());
        user.setCognome(req.cognome().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles(new HashSet<>(Set.of(ruoloUser)));
        // flush: valorizza createdAt (@CreationTimestamp) per la risposta
        userRepository.saveAndFlush(user);

        creaEInviaTokenVerifica(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void verificaEmail(String token) {
        EmailVerificationToken verifica = verificationTokenRepository.findByTokenHash(TokenUtils.sha256(token))
                .filter(t -> t.isUtilizzabile(Instant.now()))
                .orElseThrow(() -> new BadRequestException("Link di verifica non valido o scaduto"));

        Instant now = Instant.now();
        verifica.setUsedAt(now);
        User user = verifica.getUser();
        if (!user.isEmailVerificata()) {
            user.setEmailVerificataAt(now);
        }
    }

    /** Risponde sempre allo stesso modo: non rivela se l'email è registrata o già verificata. */
    @Transactional
    public void reinviaVerifica(String email) {
        userRepository.findByEmail(User.normalizzaEmail(email))
                .filter(u -> !u.isEmailVerificata())
                .ifPresent(this::creaEInviaTokenVerifica);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Optional<User> trovato = trovaPerEmailOUsername(req.emailOrUsername());

        if (trovato.isEmpty()) {
            // Stesso costo di BCrypt anche se l'utente non esiste
            passwordEncoder.matches(req.password(), hashFittizio());
            throw new InvalidCredentialsException();
        }
        User user = trovato.get();
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isEmailVerificata()) {
            throw new EmailNonVerificataException();
        }

        JwtService.TokenJwt jwt = jwtService.generaToken(user);
        return new AuthResponse(jwt.token(), jwt.expiresAt(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
    }

    private Optional<User> trovaPerEmailOUsername(String valore) {
        String v = valore.trim();
        return v.contains("@")
                ? userRepository.findByEmail(User.normalizzaEmail(v))
                : userRepository.findByUsernameIgnoreCase(v);
    }

    private void creaEInviaTokenVerifica(User user) {
        verificationTokenRepository.deleteAllByUserId(user.getId());

        String token = TokenUtils.generaToken();
        verificationTokenRepository.save(new EmailVerificationToken(
                TokenUtils.sha256(token), user, Instant.now().plus(VALIDITA_VERIFICA_EMAIL)));

        String email = user.getEmail();
        String nome = user.getNome();
        AfterCommit.run(() -> emailService.inviaVerificaEmail(email, nome, token));
    }

    private String hashFittizio() {
        if (hashFittizio == null) {
            hashFittizio = passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return hashFittizio;
    }
}
