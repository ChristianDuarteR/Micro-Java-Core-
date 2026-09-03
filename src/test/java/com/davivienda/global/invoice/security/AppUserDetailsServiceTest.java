package com.davivienda.global.invoice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Role;
import com.davivienda.global.invoice.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService service;

    @Test
    void loadsUserAuthoritiesFromRole() {
        when(userRepository.findByUsername("auditor")).thenReturn(Optional.of(
                AppUser.builder().username("auditor").password("x").role(Role.AUDITOR).build()));

        var details = service.loadUserByUsername("auditor");

        assertThat(details.getUsername()).isEqualTo("auditor");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_AUDITOR");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void throwsWhenMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
