package com.davivienda.global.invoice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Role;
import com.davivienda.global.invoice.dto.LoginRequest;
import com.davivienda.global.invoice.repository.UserRepository;
import com.davivienda.global.invoice.security.AppUserDetails;
import com.davivienda.global.invoice.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokenAndRole() {
        AppUser user = AppUser.builder().username("operador").password("hash").role(Role.OPERADOR).build();
        AppUserDetails details = new AppUserDetails(user);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        when(userRepository.findByUsername("operador")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("operador", "Operador123!"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.OPERADOR);
        assertThat(response.username()).isEqualTo("operador");
    }
}
