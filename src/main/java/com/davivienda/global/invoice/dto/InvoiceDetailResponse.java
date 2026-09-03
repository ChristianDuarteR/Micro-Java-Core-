package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.Invoice;
import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceDetailResponse(
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
        String createdBy,
        String totalInWords
) {
    public static InvoiceDetailResponse from(Invoice invoice, String totalInWords) {
        return new InvoiceDetailResponse(
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
                invoice.getCreatedBy(),
                totalInWords
        );
    }
}
