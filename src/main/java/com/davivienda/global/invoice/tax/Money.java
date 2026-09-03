package com.davivienda.global.invoice.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Money {

    private Money() {
    }

    static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal percent(BigDecimal base, String rate) {
        return scale(base.multiply(new BigDecimal(rate)));
    }
}
