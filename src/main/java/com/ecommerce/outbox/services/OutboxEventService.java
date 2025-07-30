package com.ecommerce.outbox.services;

import com.ecommerce.outbox.entities.OutboxEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface OutboxEventService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    OutboxEvent publish(OutboxEvent event);
}
