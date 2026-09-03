package com.davivienda.global.invoice.tax;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ExportacionTaxStrategy implements TaxCalculationStrategy {

    @Override
    public InvoiceType supportedType() {
        return InvoiceType.EXPORTACION;
    }

    @Override
    public TaxResult calculate(BigDecimal subtotal) {
        BigDecimal scaledSubtotal = Money.scale(subtotal);
        BigDecimal zero = Money.scale(BigDecimal.ZERO);
        return new TaxResult(zero, zero, scaledSubtotal);
    }
}
