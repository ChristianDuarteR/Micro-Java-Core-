package com.davivienda.global.invoice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.davivienda.global.invoice.dto.ClientResponse;
import com.davivienda.global.invoice.security.AppUserDetailsService;
import com.davivienda.global.invoice.security.JwtService;
import com.davivienda.global.invoice.service.ClientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void listClientsAcceptsSearchAndPagination() throws Exception {
        when(clientService.findPage(eq("Acme"), any())).thenReturn(new PageImpl<>(List.of(
                new ClientResponse(1L, "NIT", "900123456-1", "Acme Corp",
                        "facturacion@acme.test", "+57 300 000 0001", "Calle 1 # 2-3"))));

        mockMvc.perform(get("/api/clients")
                        .param("q", "Acme")
                        .param("page", "0")
                        .param("size", "5")
                        .with(user("auditor").roles("AUDITOR")))
                .andExpect(status().isOk());
    }

    @Test
    void listClientsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Expected 401 or 403 but was " + status);
                    }
                });
    }
}