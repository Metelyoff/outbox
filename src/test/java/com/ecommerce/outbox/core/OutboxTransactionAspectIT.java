package com.ecommerce.outbox.core;

import com.ecommerce.outbox.AbstractIntegrationTest;
import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.exceptions.OutboxTransactionException;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import com.ecommerce.outbox.repositories.TestRepository;
import com.ecommerce.outbox.services.TestService;
import com.ecommerce.outbox.transformers.TestPayloadTransformer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static com.ecommerce.outbox.entities.OutboxEvent.MAX_TEXT_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class OutboxTransactionAspectIT extends AbstractIntegrationTest {

    @Autowired
    private TestService testService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoSpyBean
    private OutboxEventRepository outboxEventRepositorySpy;

    @Autowired
    private TestRepository testRepository;

    @Test
    void manageTransaction_shouldThrowIfContextIdIsNotProvided() {
        assertThatThrownBy(() -> testService.withoutContextId())
                .isInstanceOf(OutboxTransactionException.class)
                .hasMessageContaining("Please provide OutboxContext ID");
    }

    @Test
    void manageTransaction_shouldUseCustomPayloadTransformer() {
        var contextId = UUID.randomUUID();
        var testEntity = testService.customTransformed(contextId::toString);

        var event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst()
                .orElseThrow();

        assertThat(event.getPayload()).isEqualTo(String.format(
                TestPayloadTransformer.FORMAT,
                testEntity.getId(),
                testEntity.getContextId()
        ));
    }

    @Test
    void manageTransaction_shouldTruncateLongFields() {
        var contextId = "123".repeat(100);
        var testEntity = testService.veryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLongVeryLong(() -> contextId.substring(0, MAX_TEXT_LENGTH));

        var event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().startsWith(contextId.substring(0, MAX_TEXT_LENGTH)))
                .findFirst()
                .orElseThrow();

        assertThat(event.getContextId().length()).isLessThanOrEqualTo(MAX_TEXT_LENGTH);
        assertThat(event.getProcessName().length()).isLessThanOrEqualTo(MAX_TEXT_LENGTH);
        assertThat(event.getEventName().length()).isLessThanOrEqualTo(MAX_TEXT_LENGTH);
        assertThat(event.getContextId()).isEqualTo(contextId.substring(0, MAX_TEXT_LENGTH));
        assertThat(event.getContextId()).isEqualTo(testEntity.getContextId().substring(0, MAX_TEXT_LENGTH));
    }

    @Test
    void manageTransaction_shouldPersistSuccessEvent() {
        var contextId = UUID.randomUUID();
        var testEntity = testService.success(contextId::toString);

        var event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst()
                .orElseThrow();

        assertThat(event.getContextId()).isEqualTo(contextId.toString());
        assertThat(event.getEventName()).isEqualTo("SuccessEvent");
        assertThat(event.getProcessName()).isEqualTo("TestService.success");
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PROCESSED);
        assertThat(event.getPayload()).isEqualTo(testEntity.toString());
    }

    @Test
    void manageTransaction_shouldPersistSuccessEventWithDoubleSave() {
        var contextId = UUID.randomUUID();
        testService.success(contextId::toString);

        assertThatThrownBy(() -> testService.success(contextId::toString))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("duplicate key value violates unique constraint");

        var eventCount = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .count();

        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void manageTransaction_shouldPublishRollbackEvent() {
        var contextId = UUID.randomUUID();

        assertThatThrownBy(() -> testService.fail(contextId::toString))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed by contextId: " + contextId);

        var shouldDoesNotPresent = testRepository.findAll().stream()
                .filter(t -> t.getContextId().equals(contextId.toString()))
                .findFirst();
        assertThat(shouldDoesNotPresent).isEmpty();

        OutboxEvent event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst()
                .orElseThrow();

        assertThat(event.getContextId()).isEqualTo(contextId.toString());
        assertThat(event.getEventName()).isEqualTo("RollbackEvent");
        assertThat(event.getProcessName()).isEqualTo("TestService.fail");
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getPayload()).isNull();
    }

    @Test
    void manageTransaction_shouldNotPublishRollbackEvent() {
        var contextId = UUID.randomUUID();

        assertThatThrownBy(() -> testService.failWithoutPublishError(contextId::toString))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed by contextId: " + contextId);

        var shouldDoesNotPresent = testRepository.findAll().stream()
                .filter(t -> t.getContextId().equals(contextId.toString()))
                .findFirst();
        assertThat(shouldDoesNotPresent).isEmpty();

        var event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst();

        assertThat(event).isNotPresent();
    }

    @Test
    void manageTransaction_shouldNotPublishRollbackEventWhenErrorWasThrown() {
        var contextId = UUID.randomUUID();

        doThrow(new RuntimeException("Test Exception"))
                .when(outboxEventRepositorySpy)
                .save(any(OutboxEvent.class));

        assertThatThrownBy(() -> testService.fail(contextId::toString))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed by contextId: " + contextId);

        var event = outboxEventRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst();
        assertThat(event).isNotPresent();

        var testEntity = testRepository.findAll().stream()
                .filter(e -> e.getContextId().equals(contextId.toString()))
                .findFirst();
        assertThat(testEntity).isNotPresent();
    }

}