package com.davivienda.global.invoice.controller;

import com.davivienda.global.invoice.dto.CreateInvoiceRequest;
import com.davivienda.global.invoice.dto.InvoiceDetailResponse;
import com.davivienda.global.invoice.dto.InvoiceResponse;
import com.davivienda.global.invoice.event.InvoiceEventHub;
import com.davivienda.global.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Facturas", description = "Alta, listado, detalle SOAP. JWT obligatorio.")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceEventHub invoiceEventHub;

    public InvoiceController(InvoiceService invoiceService, InvoiceEventHub invoiceEventHub) {
        this.invoiceService = invoiceService;
        this.invoiceEventHub = invoiceEventHub;
    }

    @PostMapping
    @Operation(
            summary = "Crear factura",
            description = "Solo OPERADOR. Calcula IVA/retención con Strategy. Notifica al micro Python de métricas. "
                    + "Si type=EXPORTACION, customsCode es obligatorio; en otros tipos se ignora."
    )
    @ApiResponse(responseCode = "201", description = "Factura persistida")
    @ApiResponse(responseCode = "400", description = "Validación o código aduanero faltante")
    @ApiResponse(responseCode = "403", description = "Rol AUDITOR u otro no autorizado")
    public ResponseEntity<InvoiceResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = {
                            @ExampleObject(name = "Nacional", value = """
                                    {
                                      "type": "NACIONAL",
                                      "subtotal": 100.00,
                                      "clientName": "Acme",
                                      "description": "Servicio"
                                    }
                                    """),
                            @ExampleObject(name = "Exportación", value = """
                                    {
                                      "type": "EXPORTACION",
                                      "subtotal": 200.00,
                                      "clientName": "Overseas Ltd",
                                      "customsCode": "ADU-001"
                                    }
                                    """),
                            @ExampleObject(name = "Gubernamental", value = """
                                    {
                                      "type": "GUBERNAMENTAL",
                                      "subtotal": 100.00,
                                      "clientName": "Ministerio"
                                    }
                                    """)
                    })
            )
            @Valid @RequestBody CreateInvoiceRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.create(request, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "Listar facturas", description = "OPERADOR y AUDITOR.")
    public List<InvoiceResponse> list() {
        return invoiceService.findAll();
    }

    @GetMapping("/{id:\\d+}")
    @Operation(
            summary = "Detalle de factura",
            description = "Incluye totalInWords desde SOAP DataFlex NumberConversion (backend → JSON)."
    )
    @ApiResponse(responseCode = "200", description = "Detalle con conversión a texto")
    @ApiResponse(responseCode = "404", description = "No existe")
    @ApiResponse(responseCode = "502", description = "Fallo del servicio SOAP")
    public InvoiceDetailResponse detail(@PathVariable Long id) {
        return invoiceService.findById(id);
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "SSE de altas (opcional)",
            description = "Evento invoice-created. Para el dashboard usa el WebSocket de Python "
                    + "ws://localhost:5000/ws/metrics?token=JWT. EventSource no envía headers: "
                    + "GET /api/invoices/events?token=<jwt>."
    )
    public SseEmitter events() {
        return invoiceEventHub.subscribe();
    }
}
