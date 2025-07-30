package com.ecommerce.outbox.transformers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboxPayloadToTypeTransformerTest {

    public record TestPayload(String name, int number) {
    }

    private OutboxPayloadToTypeTransformer<TestPayload> transformer;

    @BeforeEach
    void setUp() {
        transformer = new OutboxPayloadToTypeTransformer<>(new ObjectMapper(), new TypeReference<>() {
        });
    }

    @Test
    void transform_shouldThrowIllegalArgumentException_whenPayloadIsNull() {
        assertThatThrownBy(() -> transformer.transform(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload cannot be null");
    }

    @Test
    void transform_shouldThrowRuntimeException_whenPayloadIsNotValid() {
        var json = "{not valid}";
        assertThatThrownBy(() -> transformer.transform(json)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void transform_shouldReturnObject_whenPayloadIsNotNullAndValid() {
        var json = "{\"name\":\"name\",\"number\":1}";

        var payload = assertDoesNotThrow(() -> transformer.transform(json));

        assertNotNull(payload);
        assertEquals("name", payload.name);
        assertEquals(1, payload.number);
    }

}