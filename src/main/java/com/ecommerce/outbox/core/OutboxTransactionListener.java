package com.ecommerce.outbox.core;

import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.services.OutboxEventService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutboxTransactionListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OutboxTransactionListener.class);

    private final OutboxEventService outboxEventService;

    public OutboxTransactionListener(final OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleFailedEvent(OutboxEvent event) {
        LOG.info("Handling failed event: {}", event);
        outboxEventService.publish(event);
    }

}