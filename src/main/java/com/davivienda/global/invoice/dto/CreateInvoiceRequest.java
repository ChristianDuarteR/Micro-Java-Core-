package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.InvoiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alta de factura. customsCode solo aplica a EXPORTACION.")
public record CreateInvoiceRequest(
        @NotNull @Schema(example = "NACIONAL") InvoiceType type,
        @NotNull @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
        @Schema(example = "100.00") BigDecimal subtotal,
        @NotNull @Schema(example = "1") Long clientId,
        @Schema(example = "ADU-001") String customsCode,
        String description
) {
}
