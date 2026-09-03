package com.davivienda.global.invoice.controller;

import com.davivienda.global.invoice.dto.AuthResponse;
import com.davivienda.global.invoice.dto.LoginRequest;
import com.davivienda.global.invoice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login JWT. Público.")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Obtener JWT",
            description = "Usuarios semilla: operador / Operador123! (ROLE_OPERADOR) y auditor / Auditor123! (ROLE_AUDITOR)."
    )
    @ApiResponse(responseCode = "200", description = "Token emitido",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "username": "operador",
                              "password": "Operador123!"
                            }
                            """))
            )
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
