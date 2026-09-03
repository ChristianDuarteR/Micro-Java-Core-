package com.davivienda.global.invoice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.davivienda.global.invoice.domain.InvoiceType;
import com.davivienda.global.invoice.dto.CreateInvoiceRequest;
import com.davivienda.global.invoice.dto.InvoiceResponse;
import com.davivienda.global.invoice.event.InvoiceEventHub;
import com.davivienda.global.invoice.security.AppUserDetailsService;
import com.davivienda.global.invoice.security.JwtService;
import com.davivienda.global.invoice.service.InvoiceService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private InvoiceEventHub invoiceEventHub;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void createDelegatesToService() throws Exception {
        when(invoiceService.create(any(CreateInvoiceRequest.class), eq("operador")))
                .thenReturn(new InvoiceResponse(
                        1L, InvoiceType.NACIONAL, new BigDecimal("100.00"), new BigDecimal("19.00"),
                        new BigDecimal("0.00"), new BigDecimal("119.00"), null, "Acme", null,
                        Instant.now(), "operador"));

        mockMvc.perform(post("/api/invoices")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"NACIONAL","subtotal":100,"clientName":"Acme"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void listAndEvents() throws Exception {
        when(invoiceService.findAll()).thenReturn(List.of());
        when(invoiceEventHub.subscribe()).thenReturn(new SseEmitter(0L));

        mockMvc.perform(get("/api/invoices").with(user("auditor").roles("AUDITOR")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/invoices/events").with(user("auditor").roles("AUDITOR")))
                .andExpect(status().isOk());
        verify(invoiceEventHub).subscribe();
    }
}
