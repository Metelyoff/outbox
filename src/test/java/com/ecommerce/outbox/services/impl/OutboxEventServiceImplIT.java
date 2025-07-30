package com.ecommerce.outbox.services.impl;

import com.ecommerce.outbox.AbstractIntegrationTest;
import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import com.ecommerce.outbox.services.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OutboxEventServiceImplIT extends AbstractIntegrationTest {

    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void save_shouldPersistOutboxEventToDatabase() {
        var contextId = UUID.randomUUID().toString();
        var event = new OutboxEvent(
                contextId,
                "TestProcess",
                "TestEvent",
                "{\"itemId\":123}",
                OutboxEventStatus.PROCESSED,
                null,
                10L
        );

        var saved = outboxEventService.publish(event);

        var found = outboxEventRepository.findById(saved.getId());
        assertThat(found).isPresent();

        OutboxEvent actual = found.get();
        assertThat(actual.getContextId()).isEqualTo(contextId);
        assertThat(actual.getProcessName()).isEqualTo("TestProcess");
        assertThat(actual.getEventName()).isEqualTo("TestEvent");
        assertThat(actual.getStatus()).isEqualTo(OutboxEventStatus.PROCESSED);
        assertThat(actual.getPayload()).isEqualTo("{\"itemId\":123}");
        assertThat(actual.getStatusMessage()).isNull();
    }

    @Test
    void save_shouldGenerateSameIdIfSameContextProcessStatus() {
        var context = "ctx";
        var name = "process";

        var event1 = new OutboxEvent(
                context,
                name,
                "evt",
                "{}",
                OutboxEventStatus.PROCESSED,
                null,
                1
        );
        var event2 = new OutboxEvent(
                context,
                name,
                "evt",
                "{}",
                OutboxEventStatus.PROCESSED,
                null,
                2
        );

        var e1 = outboxEventService.publish(event1);
        var e2 = outboxEventService.publish(event2);

        assertThat(e1.getId()).isEqualTo(e2.getId());
    }

}