package com.ecommerce.outbox.transformers;

public interface Transformer<FROM, TO> {
    TO transform(FROM from);
}
