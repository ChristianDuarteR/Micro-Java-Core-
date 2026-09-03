package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.Invoice;
import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResponse(
        Long id,
        InvoiceType type,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal withholding,
        BigDecimal total,
        String customsCode,
        String clientName,
        String description,
        Instant createdAt,
        String createdBy
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getType(),
                invoice.getSubtotal(),
                invoice.getIva(),
                invoice.getWithholding(),
                invoice.getTotal(),
                invoice.getCustomsCode(),
                invoice.getClientName(),
                invoice.getDescription(),
                invoice.getCreatedAt(),
                invoice.getCreatedBy()
        );
    }
}
