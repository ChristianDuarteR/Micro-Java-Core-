package com.davivienda.global.invoice.repository;

import com.davivienda.global.invoice.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
