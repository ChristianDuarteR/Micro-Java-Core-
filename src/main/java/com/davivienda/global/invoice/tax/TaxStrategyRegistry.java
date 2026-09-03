package com.davivienda.global.invoice.tax;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TaxStrategyRegistry {

    private final Map<InvoiceType, TaxCalculationStrategy> strategies;

    public TaxStrategyRegistry(List<TaxCalculationStrategy> strategyBeans) {
        this.strategies = new EnumMap<>(InvoiceType.class);
        for (TaxCalculationStrategy strategy : strategyBeans) {
            TaxCalculationStrategy previous = strategies.put(strategy.supportedType(), strategy);
            if (previous != null) {
                throw new IllegalStateException(
                        "Hay más de una estrategia para el tipo " + strategy.supportedType());
            }
        }
    }

    public TaxResult calculate(InvoiceType type, BigDecimal subtotal) {
        TaxCalculationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No existe estrategia tributaria para el tipo: " + type);
        }
        return strategy.calculate(subtotal);
    }
}
