package com.ecommerce.outbox.annotations;

import com.ecommerce.outbox.transformers.OutboxPayloadToStringTransformer;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface OutboxTransaction {

    boolean publishError() default true;

    String successEvent() default "";

    String rollbackEvent() default "";

    Class<? extends OutboxPayloadToStringTransformer> payloadTransformer() default OutboxPayloadToStringTransformer.class;

}