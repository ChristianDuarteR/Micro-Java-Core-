package com.davivienda.global.invoice.tax;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaxEngineTest {

    private final NacionalTaxStrategy nacional = new NacionalTaxStrategy();
    private final ExportacionTaxStrategy exportacion = new ExportacionTaxStrategy();
    private final GubernamentalTaxStrategy gubernamental = new GubernamentalTaxStrategy();
    private final TaxStrategyRegistry registry =
            new TaxStrategyRegistry(List.of(nacional, exportacion, gubernamental));

    @Test
    void nacionalAddsNineteenPercentIva() {
        TaxResult result = nacional.calculate(new BigDecimal("100.00"));
        assertThat(nacional.supportedType()).isEqualTo(InvoiceType.NACIONAL);
        assertThat(result.iva()).isEqualByComparingTo("19.00");
        assertThat(result.withholding()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("119.00");
    }

    @Test
    void exportacionKeepsSubtotalWithZeroIva() {
        TaxResult result = exportacion.calculate(new BigDecimal("200"));
        assertThat(exportacion.supportedType()).isEqualTo(InvoiceType.EXPORTACION);
        assertThat(result.iva()).isEqualByComparingTo("0.00");
        assertThat(result.withholding()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("200.00");
    }

    @Test
    void gubernamentalAppliesIvaAndWithholding() {
        TaxResult result = gubernamental.calculate(new BigDecimal("100.00"));
        assertThat(gubernamental.supportedType()).isEqualTo(InvoiceType.GUBERNAMENTAL);
        assertThat(result.iva()).isEqualByComparingTo("19.00");
        assertThat(result.withholding()).isEqualByComparingTo("5.00");
        assertThat(result.total()).isEqualByComparingTo("114.00");
    }

    @Test
    void registryDelegatesByType() {
        assertThat(registry.calculate(InvoiceType.NACIONAL, new BigDecimal("10")).total())
                .isEqualByComparingTo("11.90");
        assertThat(registry.calculate(InvoiceType.EXPORTACION, new BigDecimal("10")).total())
                .isEqualByComparingTo("10.00");
        assertThat(registry.calculate(InvoiceType.GUBERNAMENTAL, new BigDecimal("10")).total())
                .isEqualByComparingTo("11.40");
    }

    @Test
    void registryRejectsDuplicateStrategies() {
        assertThatThrownBy(() -> new TaxStrategyRegistry(List.of(nacional, new NacionalTaxStrategy())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NACIONAL");
    }

    @Test
    void registryRejectsUnknownType() {
        TaxStrategyRegistry empty = new TaxStrategyRegistry(List.of());
        assertThatThrownBy(() -> empty.calculate(InvoiceType.NACIONAL, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moneyPercentRoundsHalfUp() {
        assertThat(Money.percent(new BigDecimal("10.55"), "0.19")).isEqualByComparingTo("2.00");
        assertThat(Money.scale(new BigDecimal("1.005"))).isEqualByComparingTo("1.01");
    }
}
