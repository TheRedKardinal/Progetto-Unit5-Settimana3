package com.example.be;

import com.example.be.entity.Car;
import com.example.be.entity.Carburante;
import com.example.be.entity.Condizione;
import com.example.be.entity.Role;
import com.example.be.entity.StatoAnnuncio;
import com.example.be.entity.User;
import com.example.be.repository.CarRepository;
import com.example.be.repository.EmailVerificationTokenRepository;
import com.example.be.repository.FavoriteRepository;
import com.example.be.repository.PasswordResetTokenRepository;
import com.example.be.repository.RoleRepository;
import com.example.be.repository.UserRepository;
import com.example.be.security.JwtService;
import com.example.be.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base dei test di integrazione: contesto Spring completo su H2 in memoria (condiviso tra le classi),
 * EmailService finto per verificare le email senza inviarle, pulizia dei dati dopo ogni test.
 * Niente @Transactional sui test: gli eventi AFTER_COMMIT devono vedere commit reali.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected CarRepository carRepository;
    @Autowired
    protected FavoriteRepository favoriteRepository;
    @Autowired
    protected PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    protected EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected JwtService jwtService;

    @MockitoBean
    protected EmailService emailService;

    @AfterEach
    void pulisciDati() {
        favoriteRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals("admin"))
                .forEach(userRepository::delete);
    }

    protected User creaUtente(String username, boolean verificato) {
        User user = new User();
        user.setNome("Nome");
        user.setCognome("Cognome");
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmailVerificataAt(verificato ? Instant.now() : null);
        user.setRoles(new HashSet<>(Set.of(roleRepository.findByRole(Role.USER).orElseThrow())));
        return userRepository.save(user);
    }

    protected User admin() {
        return userRepository.findByUsernameIgnoreCase("admin").orElseThrow();
    }

    protected String bearer(User user) {
        return "Bearer " + jwtService.generaToken(user).token();
    }

    protected Car creaAuto(String titolo, String marca, String modello, BigDecimal prezzo, StatoAnnuncio stato) {
        Car car = new Car();
        car.setMarca(marca);
        car.setModello(modello);
        car.setAnno(Year.of(2020));
        car.setTitolo(titolo);
        car.setDescrizione("Descrizione di test");
        car.setChilometraggio(new BigDecimal("10000.0"));
        car.setCarburante(Carburante.BENZINA);
        car.setPrezzo(prezzo);
        car.setCondizione(Condizione.USATO);
        car.setStatoAnnuncio(stato);
        car.setPublishedAt(stato == StatoAnnuncio.PUBBLICATO ? Instant.now() : null);
        car.setImmagini(new java.util.ArrayList<>(List.of("https://example.com/" + marca + ".jpg")));
        return carRepository.save(car);
    }
}
