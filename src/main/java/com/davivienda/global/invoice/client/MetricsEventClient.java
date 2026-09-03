package com.davivienda.global.invoice.client;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MetricsEventClient {

    private static final Logger log = LoggerFactory.getLogger(MetricsEventClient.class);

    private final RestClient restClient;
    private final String notifyUrl;
    private final String internalKey;

    public MetricsEventClient(
            RestClient restClient,
            @Value("${metrics.base-url}") String baseUrl,
            @Value("${metrics.internal-key}") String internalKey
    ) {
        this.restClient = restClient;
        this.notifyUrl = trimSlash(baseUrl) + "/internal/events/invoice-created";
        this.internalKey = internalKey;
    }

    public void notifyInvoiceCreated(InvoiceType type, BigDecimal total) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("invoice_type", type.name());
        body.put("total", total.toPlainString());
        log.info("Notificando factura creada a métricas. url={}, invoiceType={}, total={}",
            notifyUrl, type, total);
        try {
            var response = restClient.post()
                    .uri(notifyUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Internal-Key", internalKey)
                    .body(body)
                    .retrieve()
                .toBodilessEntity();
            log.info("Notificación de factura recibida por métricas. status={}, invoiceType={}, total={}",
                response.getStatusCode(), type, total);
        } catch (Exception ex) {
            log.error("No se pudo notificar al micro de métricas. url={}, invoiceType={}, total={}, reason={}",
                notifyUrl, type, total, ex.getMessage(), ex);
        }
    }

    private static String trimSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
