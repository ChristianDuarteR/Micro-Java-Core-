package com.davivienda.global.invoice.config;

import com.davivienda.global.invoice.security.JwtAuthenticationFilter; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Permitir PREFLIGHT (OPTIONS) globales para CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Rutas públicas de autenticación y salud
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()

                        // 3. Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-config",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 4. Cambiado a hasAuthority / hasAnyAuthority (acepta 'OPERADOR' sin requerir 'ROLE_')
                        .requestMatchers(HttpMethod.POST, "/api/invoices", "/api/invoices/**")
                            .hasAnyAuthority("OPERADOR", "ROLE_OPERADOR")
                        
                        .requestMatchers(HttpMethod.GET, "/api/invoices", "/api/invoices/**")
                            .hasAnyAuthority("OPERADOR", "ROLE_OPERADOR", "AUDITOR", "ROLE_AUDITOR")
                        
                        .requestMatchers(HttpMethod.GET, "/api/clients", "/api/clients/**")
                            .hasAnyAuthority("OPERADOR", "ROLE_OPERADOR", "AUDITOR", "ROLE_AUDITOR")

                        // 5. Cualquier otra ruta requiere autenticación básica
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGINS:https://global-invoice-virid.vercel.app}") String corsOrigins) {
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> allowedOrigins = Arrays.asList(corsOrigins.split(","));
        configuration.setAllowedOrigins(allowedOrigins);
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}