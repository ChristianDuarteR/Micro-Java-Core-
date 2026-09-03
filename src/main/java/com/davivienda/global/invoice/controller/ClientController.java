package com.davivienda.global.invoice.controller;

import com.davivienda.global.invoice.dto.ClientResponse;
import com.davivienda.global.invoice.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clientes", description = "Consulta de clientes para selección de facturas.")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(summary = "Listar y buscar clientes", description = "Consulta paginada para el dropdown de facturas.")
    public Page<ClientResponse> list(
            @RequestParam(defaultValue = "") String q,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return clientService.findPage(q, pageable);
    }
}