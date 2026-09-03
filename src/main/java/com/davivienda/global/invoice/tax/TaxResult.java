package com.davivienda.global.invoice.tax;

import java.math.BigDecimal;

public record TaxResult(BigDecimal iva, BigDecimal withholding, BigDecimal total) {
}
