package com.davivienda.global.invoice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.davivienda.global.invoice.domain.Client;
import com.davivienda.global.invoice.repository.ClientRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Test
    void findPageSearchesByNameDocumentOrEmail() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Client client = Client.builder()
                .id(1L)
                .documentType("NIT")
                .documentNumber("900123456-1")
                .name("Acme Corp")
                .email("facturacion@acme.test")
                .build();
        when(clientRepository
                .findByNameContainingIgnoreCaseOrDocumentNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "Acme", "Acme", "Acme", pageRequest))
                .thenReturn(new PageImpl<>(List.of(client), pageRequest, 1));

        var page = new ClientService(clientRepository).findPage(" Acme ", pageRequest);

        assertThat(page.getContent()).hasSize(1).first().extracting("name").isEqualTo("Acme Corp");
        verify(clientRepository)
                .findByNameContainingIgnoreCaseOrDocumentNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "Acme", "Acme", "Acme", pageRequest);
    }
}