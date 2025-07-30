package com.ecommerce.outbox.listeners;

import com.ecommerce.outbox.AbstractIntegrationTest;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.core.OutboxEventHandlerFactory;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class DefaultOutboxEventListenerIT extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, OutboxEvent> kafkaTemplate;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private OutboxEventHandlerFactory outboxEventHandlerFactory;

    @MockitoSpyBean
    private OutboxEventHandler testHandler;

    @Test
    void shouldProcessNewEventAndAcknowledge() throws Exception {
        var contextId = UUID.randomUUID().toString();
        var processName = "TestProcess";
        var status = OutboxEventStatus.PROCESSED;
        var id = UUID.nameUUIDFromBytes(String.format("%s-%s-%s", contextId, processName, status).getBytes(UTF_8));
        var event = new OutboxEvent(
                id.toString(),
                processName,
                "TestEvent",
                contextId,
                "{\"data\":\"test\"}",
                status,
                null,
                null
        );

        when(outboxEventHandlerFactory.getHandler(event)).thenReturn(testHandler);
        when(testHandler.eventName()).thenReturn("TestEvent");
        when(testHandler.status()).thenReturn(OutboxEventStatus.PROCESSED);
        doCallRealMethod().when(testHandler).handleEvent(any());

        kafkaTemplate.send("topic-1", event).get(5, TimeUnit.SECONDS);

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertTrue(outboxEventRepository.existsById(id));
        });
    }

    @Test
    void shouldSkipAlreadyProcessedEvent() throws Exception {
        var contextId = UUID.randomUUID().toString();
        var processName = "TestProcess";
        var status = OutboxEventStatus.PROCESSED;
        var id = UUID.nameUUIDFromBytes(String.format("%s-%s-%s", contextId, processName, status).getBytes(UTF_8));
        var existingEvent = new OutboxEvent(
                id.toString(),
                processName,
                "TestEvent",
                contextId,
                "{\"data\":\"already_processed\"}",
                status,
                null,
                null
        );

        outboxEventRepository.save(new com.ecommerce.outbox.entities.OutboxEvent(
                existingEvent.contextId(),
                existingEvent.processName(),
                existingEvent.eventName(),
                existingEvent.payload(),
                existingEvent.status(),
                existingEvent.statusMessage(),
                10
        ));

        kafkaTemplate.send("topic-1", existingEvent).get(5, TimeUnit.SECONDS);

        Awaitility.await().during(Duration.ofSeconds(3)).untilAsserted(() -> {
            Optional<com.ecommerce.outbox.entities.OutboxEvent> reFetched = outboxEventRepository.findById(id);
            assertTrue(reFetched.isPresent());
            assertEquals("{\"data\":\"already_processed\"}", reFetched.get().getPayload());
        });
    }

    @Test
    void shouldNotAcknowledgeIfHandlerFails() throws Exception {
        var contextId = UUID.randomUUID().toString();
        var processName = "FailProcess";
        var status = OutboxEventStatus.FAILED;
        var id = UUID.nameUUIDFromBytes(String.format("%s-%s-%s", contextId, processName, status).getBytes(UTF_8));
        var failingEvent = new OutboxEvent(
                id.toString(),
                processName,
                "TestFailEvent",
                contextId,
                "{\"data\":\"fail\"}",
                status,
                null,
                null
        );

        when(outboxEventHandlerFactory.getHandler(failingEvent)).thenReturn(testHandler);
        when(testHandler.eventName()).thenReturn("TestFailEvent");
        when(testHandler.status()).thenReturn(OutboxEventStatus.FAILED);
        doThrow(new RuntimeException("Processing failed")).when(testHandler).handleEvent(any());

        kafkaTemplate.send("topic-1", failingEvent).get(5, TimeUnit.SECONDS);

        Awaitility.await().atMost(Duration.ofSeconds(7)).untilAsserted(() -> {
            verify(testHandler, times(3)).handleEvent(any());
        });
    }

}