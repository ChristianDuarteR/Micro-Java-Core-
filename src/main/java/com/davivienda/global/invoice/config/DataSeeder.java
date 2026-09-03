package com.davivienda.global.invoice.config;

import com.davivienda.global.invoice.domain.AppUser;
import com.davivienda.global.invoice.domain.Client;
import com.davivienda.global.invoice.domain.Role;
import com.davivienda.global.invoice.repository.ClientRepository;
import com.davivienda.global.invoice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
        CommandLineRunner seedUsers(
            UserRepository userRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder
        ) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder, "operador", "Operador123!", Role.OPERADOR);
            createIfMissing(userRepository, passwordEncoder, "auditor", "Auditor123!", Role.AUDITOR);
                createClientIfMissing(clientRepository, "NIT", "900123456-1", "Acme Corp",
                    "facturacion@acme.test", "+57 300 000 0001", "Calle 1 # 2-3");
                createClientIfMissing(clientRepository, "NIT", "900123457-2", "Overseas Ltd",
                    "billing@overseas.test", "+57 300 000 0002", "Carrera 4 # 5-6");
                createClientIfMissing(clientRepository, "NIT", "900123458-3", "Ministerio",
                    "tesoreria@ministerio.test", "+57 300 000 0003", "Avenida 7 # 8-9");
        };
    }

            private void createClientIfMissing(
                ClientRepository clientRepository,
                String documentType,
                String documentNumber,
                String name,
                String email,
                String phone,
                String address
            ) {
            if (clientRepository.findByDocumentNumber(documentNumber).isEmpty()) {
                clientRepository.save(Client.builder()
                    .documentType(documentType)
                    .documentNumber(documentNumber)
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .address(address)
                    .build());
            }
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
