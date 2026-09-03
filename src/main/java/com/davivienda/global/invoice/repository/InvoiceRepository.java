package com.davivienda.global.invoice.repository;

import com.davivienda.global.invoice.domain.Invoice;
import com.davivienda.global.invoice.domain.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	Page<Invoice> findByClient_NameContainingIgnoreCase(String clientName, Pageable pageable);

	Page<Invoice> findByType(InvoiceType type, Pageable pageable);
}
