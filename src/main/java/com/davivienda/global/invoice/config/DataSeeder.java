package com.davivienda.global.invoice.config;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Role;
import com.davivienda.global.invoice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder, "operador", "Operador123!", Role.OPERADOR);
            createIfMissing(userRepository, passwordEncoder, "auditor", "Auditor123!", Role.AUDITOR);
        };
    }

    private void createIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Role role
    ) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(AppUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .build());
        }
    }
}
