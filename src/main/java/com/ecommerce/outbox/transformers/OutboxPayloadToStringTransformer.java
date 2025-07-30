package com.ecommerce.outbox.transformers;

public interface OutboxPayloadToStringTransformer<T> extends Transformer<T, String> {
    String transform(T from);
}
