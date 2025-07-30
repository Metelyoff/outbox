package com.ecommerce.outbox.core;

import com.ecommerce.outbox.AbstractIntegrationTest;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import static com.ecommerce.outbox.entities.OutboxEventStatus.FAILED;
import static com.ecommerce.outbox.entities.OutboxEventStatus.PROCESSED;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@SpringBootTest
class OutboxEventHandlerFactoryIT extends AbstractIntegrationTest {

    @Autowired
    private OutboxEventHandlerFactory factory;

    @Test
    void shouldReturnRegisteredHandler() {
        var event = new OutboxEvent(
                UUID.randomUUID().toString(),
                "ProcessName",
                "TestEvent",
                UUID.randomUUID().toString(),
                null,
                PROCESSED,
                null,
                null
        );
        OutboxEventHandler handler = factory.getHandler(event);
        assertNotNull(handler);
        assertEquals("TestEvent", handler.eventName());
        assertEquals(PROCESSED, handler.status());
    }

    @Test
    void shouldThrowWhenNoHandlerFound() {
        var event = new OutboxEvent(
                UUID.randomUUID().toString(),
                "ProcessName",
                "UnknownEvent",
                UUID.randomUUID().toString(),
                null,
                FAILED,
                null,
                null
        );
        assertThrows(UnsupportedOperationException.class, () -> factory.getHandler(event));
    }

}