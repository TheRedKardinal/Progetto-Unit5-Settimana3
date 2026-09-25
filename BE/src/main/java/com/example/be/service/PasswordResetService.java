package com.example.be.service;

import com.example.be.entity.PasswordResetToken;
import com.example.be.entity.User;
import com.example.be.exception.BadRequestException;
import com.example.be.repository.PasswordResetTokenRepository;
import com.example.be.repository.UserRepository;
import com.example.be.security.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    static final Duration VALIDITA_TOKEN = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Se l'email è registrata invia il link di reset, altrimenti non fa nulla.
     * Il controller risponde sempre 200 con lo stesso messaggio: non si rivela se l'account esiste.
     */
    @Transactional
    public void richiediReset(String email) {
        userRepository.findByEmail(User.normalizzaEmail(email)).ifPresent(user -> {
            // Un solo link valido alla volta: i precedenti vengono invalidati
            tokenRepository.deleteAllByUserId(user.getId());

            String token = TokenUtils.generaToken();
            tokenRepository.save(new PasswordResetToken(
                    TokenUtils.sha256(token), user, Instant.now().plus(VALIDITA_TOKEN)));

            String destinatario = user.getEmail();
            String nome = user.getNome();
            AfterCommit.run(() -> emailService.inviaResetPassword(
                    destinatario, nome, token, VALIDITA_TOKEN.toMinutes()));
        });
    }

    @Transactional
    public void resetPassword(String token, String nuovaPassword) {
        Instant now = Instant.now();
        PasswordResetToken reset = tokenRepository.findByTokenHash(TokenUtils.sha256(token))
                .filter(t -> t.isUtilizzabile(now))
                .orElseThrow(() -> new BadRequestException("Link di reset non valido o scaduto"));

        reset.setUsedAt(now);

        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(nuovaPassword));
        // Ha dimostrato di avere accesso alla casella: l'email è di fatto verificata
        if (!user.isEmailVerificata()) {
            user.setEmailVerificataAt(now);
        }
    }
}
