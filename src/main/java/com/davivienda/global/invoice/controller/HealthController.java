package com.davivienda.global.invoice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Liveness. Público.")
@SecurityRequirements
public class HealthController {

    @GetMapping
    @Operation(summary = "Estado del servicio")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
