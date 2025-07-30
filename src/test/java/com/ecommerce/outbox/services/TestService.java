package com.ecommerce.outbox.services;

import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.TestEntity;
import com.ecommerce.outbox.repositories.TestRepository;
import com.ecommerce.outbox.transformers.TestPayloadTransformer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TestService {

    private final TestRepository repository;

    public TestService(TestRepository repository) {
        this.repository = repository;
    }

    @OutboxTransaction(successEvent = "SuccessEvent", rollbackEvent = "RollbackEvent")
    public TestEntity success(OutboxContext context) {
        return repository.save(new TestEntity(UUID.randomUUID(), context.getContextId()));
    }

    @OutboxTransaction(successEvent = "SuccessEvent", rollbackEvent = "RollbackEvent")
    public void fail(OutboxContext context) {
        repository.save(new TestEntity(UUID.randomUUID(), context.getContextId()));
        throw new RuntimeException("Failed by contextId: " + context.getContextId());
    }

    @OutboxTransaction(publishError = false)
    public void failWithoutPublishError(OutboxContext context) {
        repository.save(new TestEntity(UUID.randomUUID(), context.getContextId()));
        throw new RuntimeException("Failed by contextId: " + context.getContextId());
    }

    @OutboxTransaction(successEvent = "SuccessEvent", rollbackEvent = "RollbackEvent")
    public TestEntity withoutContextId() {
        return repository.save(new TestEntity(UUID.randomUUID(), UUID.randomUUID().toString()));
    }

    @OutboxTransaction(payloadTransformer = TestPayloadTransformer.class)
    public TestEntity customTransformed(OutboxContext context) {
        return repository.save(new TestEntity(UUID.randomUUID(), context.getContextId()));
    }

    @OutboxTransaction(
            successEvent = "veryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLong",
            rollbackEvent = "veryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLong"
    )
    public TestEntity veryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLong(OutboxContext context) {
        return repository.save(new TestEntity(UUID.randomUUID(), context.getContextId()));
    }

}
