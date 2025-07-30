package com.ecommerce.outbox.core;

import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.ecommerce.outbox.entities.OutboxEventStatus.PROCESSED;

@TestConfiguration
public class TestHandlerConfig {

    @Bean
    OutboxEventHandler testHandler(final OutboxEventRepository repository) {
        return new OutboxEventHandler() {

            @Override
            public void handleEvent(OutboxEvent event) {
                repository.save(new com.ecommerce.outbox.entities.OutboxEvent(
                        event.contextId(),
                        event.processName(),
                        event.eventName(),
                        event.payload(),
                        event.status(),
                        event.statusMessage(),
                        10
                ));
            }

            @Override
            public String eventName() {
                return "TestEvent";
            }

            @Override
            public OutboxEventStatus status() {
                return PROCESSED;
            }
        };
    }

}
