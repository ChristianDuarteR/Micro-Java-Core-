package com.davivienda.global.invoice.tax;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class GubernamentalTaxStrategy implements TaxCalculationStrategy {

    private static final String IVA_RATE = "0.19";
    private static final String WITHHOLDING_RATE = "0.05";

    @Override
    public InvoiceType supportedType() {
        return InvoiceType.GUBERNAMENTAL;
    }

    @Override
    public TaxResult calculate(BigDecimal subtotal) {
        BigDecimal scaledSubtotal = Money.scale(subtotal);
        BigDecimal iva = Money.percent(scaledSubtotal, IVA_RATE);
        BigDecimal withholding = Money.percent(scaledSubtotal, WITHHOLDING_RATE);
        BigDecimal total = scaledSubtotal.add(iva).subtract(withholding);
        return new TaxResult(iva, withholding, total);
    }
}
