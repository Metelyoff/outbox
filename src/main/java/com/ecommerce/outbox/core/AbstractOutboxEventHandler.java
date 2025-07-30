package com.ecommerce.outbox.core;

import com.ecommerce.outbox.entities.OutboxEventStatus;

public abstract class AbstractOutboxEventHandler implements OutboxEventHandler {

    private final String name;
    private final OutboxEventStatus status;

    public AbstractOutboxEventHandler(final String name, final OutboxEventStatus status) {
        this.name = name;
        this.status = status;
    }

    @Override
    public String eventName() {
        return name;
    }

    @Override
    public OutboxEventStatus status() {
        return status;
    }

}
