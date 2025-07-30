package com.ecommerce.outbox.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboxPayloadToTypeTransformer<T> implements Transformer<String, T> {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxPayloadToTypeTransformer.class);

    private final ObjectMapper mapper;
    private final TypeReference<T> typeRef;

    public OutboxPayloadToTypeTransformer(
            final ObjectMapper mapper,
            final TypeReference<T> typeRef
    ) {
        this.mapper = mapper;
        this.typeRef = typeRef;
    }

    @Override
    public T transform(String payload) {
        LOG.debug("Transforming outbox payload: {}", payload);
        if (payload == null) throw new IllegalArgumentException("Payload cannot be null");
        try {
            return mapper.readValue(payload, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
