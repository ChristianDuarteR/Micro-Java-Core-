package com.davivienda.global.invoice.service;

import com.davivienda.global.invoice.client.MetricsEventClient;
import com.davivienda.global.invoice.domain.Invoice;
import com.davivienda.global.invoice.domain.InvoiceType;
import com.davivienda.global.invoice.dto.CreateInvoiceRequest;
import com.davivienda.global.invoice.dto.InvoiceCreatedEvent;
import com.davivienda.global.invoice.dto.InvoiceDetailResponse;
import com.davivienda.global.invoice.dto.InvoiceResponse;
import com.davivienda.global.invoice.event.InvoiceEventHub;
import com.davivienda.global.invoice.exception.BusinessException;
import com.davivienda.global.invoice.exception.ResourceNotFoundException;
import com.davivienda.global.invoice.repository.InvoiceRepository;
import com.davivienda.global.invoice.soap.NumberConversionClient;
import com.davivienda.global.invoice.tax.TaxResult;
import com.davivienda.global.invoice.tax.TaxStrategyRegistry;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final TaxStrategyRegistry taxStrategyRegistry;
    private final NumberConversionClient numberConversionClient;
    private final InvoiceEventHub invoiceEventHub;
    private final MetricsEventClient metricsEventClient;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            TaxStrategyRegistry taxStrategyRegistry,
            NumberConversionClient numberConversionClient,
            InvoiceEventHub invoiceEventHub,
            MetricsEventClient metricsEventClient
    ) {
        this.invoiceRepository = invoiceRepository;
        this.taxStrategyRegistry = taxStrategyRegistry;
        this.numberConversionClient = numberConversionClient;
        this.invoiceEventHub = invoiceEventHub;
        this.metricsEventClient = metricsEventClient;
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request, String createdBy) {
        validateCustomsCode(request);
        var subtotal = request.subtotal().setScale(2, RoundingMode.HALF_UP);
        TaxResult tax = taxStrategyRegistry.calculate(request.type(), subtotal);
        Invoice invoice = Invoice.builder()
                .type(request.type())
                .subtotal(subtotal)
                .iva(tax.iva())
                .withholding(tax.withholding())
                .total(tax.total())
                .customsCode(request.type() == InvoiceType.EXPORTACION ? request.customsCode() : null)
                .clientName(request.clientName().trim())
                .description(request.description())
                .createdAt(Instant.now())
                .createdBy(createdBy)
                .build();
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Factura persistida. id={}, type={}, total={}; enviando evento a métricas",
            saved.getId(), saved.getType(), saved.getTotal());
        invoiceEventHub.publish(new InvoiceCreatedEvent(
                saved.getId(), saved.getType(), saved.getTotal(), saved.getCreatedAt()));
        metricsEventClient.notifyInvoiceCreated(saved.getType(), saved.getTotal());
        log.info("Flujo de creación de factura completado. id={}", saved.getId());
        return InvoiceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream().map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> findPage(String query, Pageable pageable) {
        String normalizedQuery = query == null ? "" : query.trim();
        Page<Invoice> invoices;
        if (normalizedQuery.isBlank()) {
            invoices = invoiceRepository.findAll(pageable);
        } else {
            InvoiceType type = parseType(normalizedQuery);
            invoices = type == null
                    ? invoiceRepository.findByClientNameContainingIgnoreCase(normalizedQuery, pageable)
                    : invoiceRepository.findByType(type, pageable);
        }
        return invoices.map(InvoiceResponse::from);
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse findById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + id));
        String totalInWords = numberConversionClient.toWords(invoice.getTotal());
        return InvoiceDetailResponse.from(invoice, totalInWords);
    }

    private void validateCustomsCode(CreateInvoiceRequest request) {
        if (request.type() == InvoiceType.EXPORTACION
                && (request.customsCode() == null || request.customsCode().isBlank())) {
            throw new BusinessException("El código aduanero es obligatorio para facturas de exportación");
        }
    }

    private InvoiceType parseType(String query) {
        try {
            return InvoiceType.valueOf(query.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
