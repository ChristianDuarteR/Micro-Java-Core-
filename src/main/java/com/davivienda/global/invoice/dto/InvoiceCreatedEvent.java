package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceCreatedEvent(
        Long id,
        InvoiceType type,
        BigDecimal total,
        Instant createdAt
) {
}
