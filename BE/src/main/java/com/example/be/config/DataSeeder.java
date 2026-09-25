package com.example.be.config;

import com.example.be.entity.Role;
import com.example.be.entity.User;
import com.example.be.repository.RoleRepository;
import com.example.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

/**
 * All'avvio crea i ruoli USER e ADMIN e il super admin (se non esistono già).
 * La password dell'admin esistente non viene mai sovrascritta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role user = trovaOCreaRuolo(Role.USER);
        Role admin = trovaOCreaRuolo(Role.ADMIN);
        creaSuperAdmin(user, admin);
    }

    private Role trovaOCreaRuolo(String nome) {
        return roleRepository.findByRole(nome)
                .orElseGet(() -> roleRepository.save(new Role(nome)));
    }

    private void creaSuperAdmin(Role user, Role admin) {
        AppProperties.Admin conf = props.admin();
        String email = User.normalizzaEmail(conf.email());

        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(conf.username())) {
            log.info("Super admin già presente ({})", email);
            return;
        }
        if (conf.password() == null || conf.password().isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD mancante: impostala in BE/.env per creare il super admin al primo avvio");
        }

        User superAdmin = new User();
        superAdmin.setNome(conf.nome());
        superAdmin.setCognome(conf.cognome());
        superAdmin.setUsername(conf.username());
        superAdmin.setEmail(email);
        superAdmin.setPassword(passwordEncoder.encode(conf.password()));
        superAdmin.setEmailVerificataAt(Instant.now());
        superAdmin.setRoles(Set.of(user, admin));
        userRepository.save(superAdmin);

        log.info("Super admin creato: {}", email);
    }
}
