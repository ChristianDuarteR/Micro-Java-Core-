package com.davivienda.global.invoice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.davivienda.global.invoice.client.MetricsEventClient;
import com.davivienda.global.invoice.domain.Invoice;
import com.davivienda.global.invoice.domain.InvoiceType;
import com.davivienda.global.invoice.dto.CreateInvoiceRequest;
import com.davivienda.global.invoice.dto.InvoiceCreatedEvent;
import com.davivienda.global.invoice.event.InvoiceEventHub;
import com.davivienda.global.invoice.exception.BusinessException;
import com.davivienda.global.invoice.exception.ResourceNotFoundException;
import com.davivienda.global.invoice.repository.InvoiceRepository;
import com.davivienda.global.invoice.soap.NumberConversionClient;
import com.davivienda.global.invoice.tax.ExportacionTaxStrategy;
import com.davivienda.global.invoice.tax.GubernamentalTaxStrategy;
import com.davivienda.global.invoice.tax.NacionalTaxStrategy;
import com.davivienda.global.invoice.tax.TaxStrategyRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private NumberConversionClient numberConversionClient;
    @Mock
    private InvoiceEventHub invoiceEventHub;
    @Mock
    private MetricsEventClient metricsEventClient;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        TaxStrategyRegistry registry = new TaxStrategyRegistry(
                List.of(new NacionalTaxStrategy(), new ExportacionTaxStrategy(), new GubernamentalTaxStrategy()));
        invoiceService = new InvoiceService(
                invoiceRepository, registry, numberConversionClient, invoiceEventHub, metricsEventClient);
    }

    @Test
    void createNacionalPublishesEvent() {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });
        var request = new CreateInvoiceRequest(
                InvoiceType.NACIONAL, new BigDecimal("100"), "Acme", null, "Servicio");

        var response = invoiceService.create(request, "operador");

        assertThat(response.total()).isEqualByComparingTo("119.00");
        assertThat(response.customsCode()).isNull();
        ArgumentCaptor<InvoiceCreatedEvent> captor = ArgumentCaptor.forClass(InvoiceCreatedEvent.class);
        verify(invoiceEventHub).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(InvoiceType.NACIONAL);
        verify(metricsEventClient).notifyInvoiceCreated(InvoiceType.NACIONAL, response.total());
    }

    @Test
    void exportRequiresCustomsCode() {
        var request = new CreateInvoiceRequest(
                InvoiceType.EXPORTACION, new BigDecimal("100"), "Acme", " ", null);
        assertThatThrownBy(() -> invoiceService.create(request, "operador"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void exportStoresCustomsCodeAndIgnoresItForOtherTypes() {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });
        var export = invoiceService.create(
                new CreateInvoiceRequest(InvoiceType.EXPORTACION, new BigDecimal("50"), "Acme", "ADU-1", null),
                "operador");
        assertThat(export.customsCode()).isEqualTo("ADU-1");
        assertThat(export.total()).isEqualByComparingTo("50.00");

        var nacional = invoiceService.create(
                new CreateInvoiceRequest(InvoiceType.NACIONAL, new BigDecimal("50"), "Acme", "SHOULD-DROP", null),
                "operador");
        assertThat(nacional.customsCode()).isNull();
    }

    @Test
    void findAllMapsEntities() {
        Invoice invoice = Invoice.builder()
                .id(7L)
                .type(InvoiceType.GUBERNAMENTAL)
                .subtotal(new BigDecimal("100.00"))
                .iva(new BigDecimal("19.00"))
                .withholding(new BigDecimal("5.00"))
                .total(new BigDecimal("114.00"))
                .clientName("Gov")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .createdBy("operador")
                .build();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        assertThat(invoiceService.findAll()).hasSize(1).first().extracting("id").isEqualTo(7L);
    }

    @Test
    void findPageSearchesByClient() {
        Invoice invoice = Invoice.builder()
                .id(8L)
                .type(InvoiceType.NACIONAL)
                .subtotal(new BigDecimal("100.00"))
                .iva(new BigDecimal("19.00"))
                .withholding(new BigDecimal("0.00"))
                .total(new BigDecimal("119.00"))
                .clientName("Acme Corp")
                .createdAt(Instant.now())
                .createdBy("operador")
                .build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(invoiceRepository.findByClientNameContainingIgnoreCase("Acme", pageRequest))
                .thenReturn(new PageImpl<>(List.of(invoice), pageRequest, 1));

        var page = invoiceService.findPage(" Acme ", pageRequest);

        assertThat(page.getContent()).hasSize(1).first().extracting("clientName").isEqualTo("Acme Corp");
        verify(invoiceRepository).findByClientNameContainingIgnoreCase("Acme", pageRequest);
    }

    @Test
    void findPageSearchesByType() {
        PageRequest pageRequest = PageRequest.of(1, 5);
        when(invoiceRepository.findByType(InvoiceType.EXPORTACION, pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        var page = invoiceService.findPage("exportacion", pageRequest);

        assertThat(page).isEmpty();
        verify(invoiceRepository).findByType(InvoiceType.EXPORTACION, pageRequest);
    }

    @Test
    void detailCallsSoap() {
        Invoice invoice = Invoice.builder()
                .id(3L)
                .type(InvoiceType.NACIONAL)
                .subtotal(new BigDecimal("150.00"))
                .iva(new BigDecimal("28.50"))
                .withholding(new BigDecimal("0.00"))
                .total(new BigDecimal("178.50"))
                .clientName("Acme")
                .createdAt(Instant.now())
                .createdBy("operador")
                .build();
        when(invoiceRepository.findById(3L)).thenReturn(Optional.of(invoice));
        when(numberConversionClient.toWords(invoice.getTotal())).thenReturn("one hundred seventy eight");

        var detail = invoiceService.findById(3L);

        assertThat(detail.totalInWords()).isEqualTo("one hundred seventy eight");
    }

    @Test
    void detailNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> invoiceService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
