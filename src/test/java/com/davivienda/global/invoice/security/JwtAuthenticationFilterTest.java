package com.davivienda.global.invoice.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Role;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private AppUserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesFromBearerHeader() throws Exception {
        AppUserDetails details = new AppUserDetails(AppUser.builder()
                .username("operador").password("x").role(Role.OPERADOR).build());
        when(jwtService.extractUsername("abc")).thenReturn("operador");
        when(userDetailsService.loadUserByUsername("operador")).thenReturn(details);
        when(jwtService.isValid("abc", details)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void ignoresInvalidToken() throws Exception {
        when(jwtService.extractUsername("bad")).thenThrow(new RuntimeException("invalid"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        org.assertj.core.api.Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void continuesWithoutToken() throws Exception {
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);
        verify(jwtService, never()).extractUsername(eq("x"));
        verify(filterChain).doFilter(any(), any());
    }
}
