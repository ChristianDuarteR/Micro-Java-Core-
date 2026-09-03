package com.davivienda.global.invoice.event;

import com.davivienda.global.invoice.dto.InvoiceCreatedEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class InvoiceEventHub {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L) {
            @Override
            public void complete() {
                emitters.remove(this);
                super.complete();
            }
        };
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        return emitter;
    }

    public void publish(InvoiceCreatedEvent event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("invoice-created").data(event));
            } catch (IOException ex) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    int subscriberCount() {
        return emitters.size();
    }
}
