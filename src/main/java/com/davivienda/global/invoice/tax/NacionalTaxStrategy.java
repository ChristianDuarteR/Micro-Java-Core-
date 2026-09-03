package com.davivienda.global.invoice.tax;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NacionalTaxStrategy implements TaxCalculationStrategy {

    private static final String IVA_RATE = "0.19";

    @Override
    public InvoiceType supportedType() {
        return InvoiceType.NACIONAL;
    }

    @Override
    public TaxResult calculate(BigDecimal subtotal) {
        BigDecimal scaledSubtotal = Money.scale(subtotal);
        BigDecimal iva = Money.percent(scaledSubtotal, IVA_RATE);
        BigDecimal withholding = Money.scale(BigDecimal.ZERO);
        return new TaxResult(iva, withholding, scaledSubtotal.add(iva));
    }
}
