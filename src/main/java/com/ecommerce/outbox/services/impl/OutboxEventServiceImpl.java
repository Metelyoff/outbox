package com.ecommerce.outbox.services.impl;

import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import com.ecommerce.outbox.services.OutboxEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private static final Logger LOG = LoggerFactory.getLogger(OutboxEventServiceImpl.class);

    public OutboxEventServiceImpl(
            OutboxEventRepository outboxEventRepository
    ) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public OutboxEvent publish(OutboxEvent event) {
        LOG.debug("Persist event: {}", event);
        return outboxEventRepository.save(event);
    }

}
