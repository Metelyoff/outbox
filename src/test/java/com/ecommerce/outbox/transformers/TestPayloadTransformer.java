package com.ecommerce.outbox.transformers;

import com.ecommerce.outbox.entities.TestEntity;
import org.springframework.stereotype.Component;

@Component
public class TestPayloadTransformer implements OutboxPayloadToStringTransformer<TestEntity> {

    public static final String FORMAT = "{\"id\":\"%s\",\"contextId\":\"%s\"}";

    @Override
    public String transform(TestEntity from) {
        return String.format(FORMAT, from.getId(), from.getContextId());
    }
}
