package com.davivienda.global.invoice.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test_secret_key_global_invoice_must_be_32_chars", 3_600_000);

    @Test
    void generatesAndValidatesToken() {
        UserDetails user = new AppUserDetails(AppUser.builder()
                .username("operador")
                .password("hash")
                .role(Role.OPERADOR)
                .build());

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("operador");
        assertThat(jwtService.isValid(token, user)).isTrue();
    }

    @Test
    void invalidWhenUsernameDoesNotMatch() {
        UserDetails operador = new AppUserDetails(AppUser.builder()
                .username("operador").password("hash").role(Role.OPERADOR).build());
        UserDetails auditor = new AppUserDetails(AppUser.builder()
                .username("auditor").password("hash").role(Role.AUDITOR).build());

        String token = jwtService.generateToken(operador);

        assertThat(jwtService.isValid(token, auditor)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService shortLived = new JwtService("test_secret_key_global_invoice_must_be_32_chars", -1_000);
        UserDetails user = new AppUserDetails(AppUser.builder()
                .username("operador").password("hash").role(Role.OPERADOR).build());
        String token = shortLived.generateToken(user);
        assertThat(shortLived.isValid(token, user)).isFalse();
    }
}
