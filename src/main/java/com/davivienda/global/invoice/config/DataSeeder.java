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

            createClientIfMissing(clientRepository, "NIT", "900123459-4", "Acme Corp",
                "facturacion@acme.test", "+57 300 000 0001", "Calle 1 # 2-3");
            createClientIfMissing(clientRepository, "NIT", "900123457-2", "Overseas Ltd",
                "billing@overseas.test", "+57 300 000 0002", "Carrera 4 # 5-6");
            createClientIfMissing(clientRepository, "NIT", "900123458-3", "Ministerio",
                "tesoreria@ministerio.test", "+57 300 000 0003", "Avenida 7 # 8-9");

            createClientIfMissing(clientRepository, "NIT", "900123456-1", "Bancolombia S.A.",
                    "contacto@bancolombia.com.co", "6045109000", "Carrera 48 # 26-85, Medellín");
            createClientIfMissing(clientRepository, "NIT", "860002964-4", "Grupo Nutresa S.A.",
                    "servicio.cliente@nutresa.com", "6043655600", "Carrera 52 # 14-30, Medellín");
                createClientIfMissing(clientRepository, "NIT", "890900050-1", "Almacenes Éxito S.A.",
                    "atencion.cliente@exito.com", "6043396565", "Carrera 48 # 32B Sur-139, Envigado");
            createClientIfMissing(clientRepository, "NIT", "860005224-1", "Ecopetrol S.A.",
                    "atencionconexion@ecopetrol.com.co", "6012344000", "Carrera 13 # 36-24, Bogotá");
                createClientIfMissing(clientRepository, "NIT", "890901352-3", "Cervecería Bavaria S.A.",
                    "servicioalcliente@bavaria.co", "6016389000", "Carrera 53A # 127-35, Bogotá");
                createClientIfMissing(clientRepository, "NIT", "800197268-1", "Cementos Argos S.A.",
                    "contactoargos@argos.com.co", "6043198700", "Calle 7 Sur # 42-70, Medellín");
                createClientIfMissing(clientRepository, "NIT", "860003020-1", "Organización Terpel S.A.",
                    "servicioalcliente@terpel.com", "6013172000", "Carrera 7 # 75-51, Bogotá");
                createClientIfMissing(clientRepository, "NIT", "890300279-0", "Carvajal S.A.",
                    "servicioalcliente@carvajal.com", "6026850000", "Calle 29 Norte # 6A-40, Cali");
            createClientIfMissing(clientRepository, "NIT", "800055490-6", "Claro Colombia (Comcel S.A.)",
                    "atencion.corporativa@claro.com.co", "6017420000", "Carrera 68A # 24B-10, Bogotá");
            createClientIfMissing(clientRepository, "NIT", "890900943-1",
                "Colombiana de Comercio S.A. (Corbeta/Alkosto)",
                    "servicio@alkosto.com", "6014046930", "Carrera 68 # 72-43, Bogotá");
            createClientIfMissing(clientRepository, "NIT", "890900608-9",
                    "ISA - Interconexión Eléctrica S.A.",
                    "contacto@isa.com.co", "6043157000", "Calle 12 Sur # 18-168, Medellín");
            createClientIfMissing(clientRepository, "NIT", "890902978-2", "SURA Colombia S.A.",
                    "atencion@sura.com.co", "6044378888", "Carrera 643A # 48-12, Medellín");
            createClientIfMissing(clientRepository, "NIT", "860007322-9",
                    "Empresas Públicas de Medellín E.S.P. (EPM)",
                    "epm@epm.com.co", "6043805555", "Carrera 58 # 42-125, Medellín");
                createClientIfMissing(clientRepository, "NIT", "890300559-7", "Tecnoquímicas S.A.",
                "contacto@tq.com.co", "6025240000", "Calle 23 # 7-39, Cali");
            createClientIfMissing(clientRepository, "NIT", "890300021-8", "Manuelita S.A.",
                "info@manuelita.com", "6026855000", "Carrera 4 # 10-44, Cali");
            createClientIfMissing(clientRepository, "NIT", "860003582-7", "Postobon S.A.",
                    "servicioalcliente@postobon.com.co", "6042655155", "Calle 52 # 47-42, Medellín");
                createClientIfMissing(clientRepository, "NIT", "860006373-3", "Avianca S.A.",
                    "contacto.empresas@avianca.com", "6014013434", "Calle 26 # 59-15, Bogotá");
            createClientIfMissing(clientRepository, "NIT", "890900823-1", "Grupo Familia S.A.",
                "contacto@grupo-familia.com", "6043606060", "Carrera 50 # 8 Sur-50, Medellin");
            createClientIfMissing(clientRepository, "NIT", "860069804-0", "Telefonica Movistar Colombia",
                    "corporativo@movistar.co", "6017050000", "Transversal 60 # 114A-55, Bogotá");
            createClientIfMissing(clientRepository, "NIT", "860002400-8",
                "Alpina Productos Alimenticios S.A.",
                "atencioncliente@alpina.com", "6014238600", "Carrera 85D # 46A-35, Bogota");
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
