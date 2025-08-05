package com.ecommerce.outbox.listeners;

import com.ecommerce.outbox.core.AcknowledgmentEventListener;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.core.OutboxEventHandlerFactory;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AcknowledgmentOutboxEventListener implements AcknowledgmentEventListener<OutboxEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AcknowledgmentOutboxEventListener.class);

    private final OutboxEventHandlerFactory outboxEventHandlerFactory;
    private final OutboxEventRepository outboxEventRepository;

    public AcknowledgmentOutboxEventListener(
            final OutboxEventHandlerFactory outboxEventHandlerFactory,
            final OutboxEventRepository outboxEventRepository
    ) {
        this.outboxEventHandlerFactory = outboxEventHandlerFactory;
        this.outboxEventRepository = outboxEventRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = "#{'${spring.application.outbox.listener.topics}'.split(',')}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Override
    public void onEvent(OutboxEvent event, Acknowledgment acknowledgment) {
        LOG.info("Received message: {}", event);
        try {
            if (outboxEventRepository.existsById(UUID.fromString(event.id()))) {
                LOG.warn("Event already processed: {}", event);
                return;
            }
            OutboxEventHandler handler = outboxEventHandlerFactory.getHandler(event);
            handler.handleEvent(event);
            acknowledgment.acknowledge();
            LOG.info("Event processed: {}", event);
        } catch (Exception e) {
            LOG.error("Error processed message: ", e);
            throw e;
        }
    }

}
