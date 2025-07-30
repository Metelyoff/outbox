package com.ecommerce.outbox.exceptions;

public class OutboxTransactionException extends RuntimeException {
    public OutboxTransactionException(String message) {
        super(message);
    }
}
