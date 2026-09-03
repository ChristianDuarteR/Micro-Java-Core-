package com.davivienda.global.invoice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI globalInvoiceOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Login en POST /api/auth/login y pega el token aquí.");
        return new OpenAPI()
                .info(new Info()
                        .title("Global-Invoice Core")
                        .version("1.0.0")
                        .description("""
                                CRUD de facturas, motor tributario (Strategy) y SOAP NumberConversion.
                                Roles: OPERADOR (crear + listar) y AUDITOR (listar + detalle).
                                El dashboard en tiempo real vive en el micro Python (WebSocket), no aquí.
                                """))
                .servers(List.of(new Server().url("/").description("Este servicio")))
                .components(new Components().addSecuritySchemes("bearer-jwt", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
