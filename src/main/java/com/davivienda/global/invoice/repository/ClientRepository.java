package com.davivienda.global.invoice.repository;

import com.davivienda.global.invoice.domain.Client;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByDocumentNumber(String documentNumber);

    Page<Client> findByNameContainingIgnoreCaseOrDocumentNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String documentNumber, String email, Pageable pageable);
}