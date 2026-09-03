package com.davivienda.global.invoice.tax;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;

public interface TaxCalculationStrategy {

    InvoiceType supportedType();

    TaxResult calculate(BigDecimal subtotal);
}
