package com.davivienda.global.invoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 32)
    private InvoiceType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal withholding;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "customs_code")
    private String customsCode;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;
}
