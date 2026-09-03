package com.davivienda.global.invoice.service;

import com.davivienda.global.invoice.domain.Client;
import com.davivienda.global.invoice.dto.ClientResponse;
import com.davivienda.global.invoice.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> findPage(String query, Pageable pageable) {
        String normalizedQuery = query == null ? "" : query.trim();
        Page<Client> clients = normalizedQuery.isBlank()
                ? clientRepository.findAll(pageable)
                : clientRepository
                        .findByNameContainingIgnoreCaseOrDocumentNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                normalizedQuery, normalizedQuery, normalizedQuery, pageable);
        return clients.map(ClientResponse::from);
    }
}