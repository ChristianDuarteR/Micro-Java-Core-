package com.davivienda.global.invoice.dto;

import com.davivienda.global.invoice.domain.Client;

public record ClientResponse(
        Long id,
        String documentType,
        String documentNumber,
        String name,
        String email,
        String phone,
        String address
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getDocumentType(),
                client.getDocumentNumber(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress()
        );
    }
}