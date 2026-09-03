package com.davivienda.global.invoice.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.davivienda.global.invoice.domain.InvoiceType;
import com.davivienda.global.invoice.dto.InvoiceCreatedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class InvoiceEventHubTest {

    @Test
    void subscribeAndPublishKeepEmitterAlive() {
        InvoiceEventHub hub = new InvoiceEventHub();
        SseEmitter emitter = hub.subscribe();
        assertThat(hub.subscriberCount()).isEqualTo(1);

        hub.publish(new InvoiceCreatedEvent(1L, InvoiceType.NACIONAL, new BigDecimal("119.00"), Instant.now()));
        assertThat(emitter).isNotNull();
        assertThat(hub.subscriberCount()).isEqualTo(1);

        emitter.complete();
        assertThat(hub.subscriberCount()).isEqualTo(0);
    }
}
