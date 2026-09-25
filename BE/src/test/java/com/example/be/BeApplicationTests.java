package com.example.be;

import com.example.be.entity.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeApplicationTests extends IntegrationTestBase {

    @Test
    void contextLoads() {
    }

    @Test
    void ilSeederCreaSuperAdminVerificatoConEntrambiIRuoli() {
        var admin = admin();
        assertThat(admin.isEmailVerificata()).isTrue();
        assertThat(admin.getRoles()).extracting(Role::getRole).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(passwordEncoder.matches("admin-password-test", admin.getPassword())).isTrue();
    }
}
