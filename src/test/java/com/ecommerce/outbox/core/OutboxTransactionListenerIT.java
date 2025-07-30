package com.ecommerce.outbox.core;

import com.ecommerce.outbox.AbstractIntegrationTest;
import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import com.ecommerce.outbox.services.OutboxEventService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OutboxTransactionListenerIT extends AbstractIntegrationTest {

    @Autowired
    private FailingService failingService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldPersistOutboxEventAfterRollback() {
        String contextId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> failingService.failWithRollback(contextId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error!");

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);

        OutboxEvent event = events.get(0);
        assertThat(event.getContextId()).isEqualTo(contextId);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getStatusMessage()).contains("Error!");
    }

    @TestConfiguration
    static class ImportTestConfig {

        @Bean
        public OutboxTransactionListener outboxTransactionListener(OutboxEventService service) {
            return new OutboxTransactionListener(service);
        }

        @Bean
        public FailingService failingService(ApplicationEventPublisher publisher) {
            return new FailingService(publisher);
        }
    }

    static class FailingService {

        private final ApplicationEventPublisher publisher;

        public FailingService(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @Transactional
        public void failWithRollback(String contextId) {
            OutboxEvent failedEvent = new OutboxEvent(
                    contextId,
                    "OrderService",
                    "OrderFailed",
                    null,
                    OutboxEventStatus.FAILED,
                    "Error!",
                    10
            );

            publisher.publishEvent(failedEvent);

            throw new RuntimeException("Error!");
        }
    }

}