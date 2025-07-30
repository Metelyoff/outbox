package com.ecommerce.outbox.core;

import org.springframework.kafka.support.Acknowledgment;

public interface AcknowledgmentEventListener<T> {
    void onEvent(T event, Acknowledgment acknowledgment);
}
