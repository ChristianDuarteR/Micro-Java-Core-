package com.davivienda.global.invoice.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.davivienda.global.invoice.domain.InvoiceType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MetricsEventClientTest {

    private MetricsEventClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new MetricsEventClient(builder.build(), "http://python_metrics_ms:5000/", "secret-key");
    }

    @Test
    void postsInvoiceCreatedWithInternalKey() {
        server.expect(requestTo("http://python_metrics_ms:5000/internal/events/invoice-created"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Key", "secret-key"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"invoice_type\":\"NACIONAL\",\"total\":\"119.00\"}"))
                .andRespond(withSuccess());

        client.notifyInvoiceCreated(InvoiceType.NACIONAL, new BigDecimal("119.00"));
        server.verify();
    }

    @Test
    void swallowsPythonErrorsSoInvoiceStillSucceeds() {
        server.expect(requestTo("http://python_metrics_ms:5000/internal/events/invoice-created"))
                .andRespond(withServerError());

        client.notifyInvoiceCreated(InvoiceType.EXPORTACION, new BigDecimal("50.00"));
        server.verify();
    }
}
