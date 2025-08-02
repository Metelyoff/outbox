package com.ecommerce.outbox;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConfigurationPropertiesScan(basePackages = "com.ecommerce.outbox")
@EnableJpaRepositories(basePackages = "com.ecommerce.outbox.repositories")
@ComponentScan(basePackages = "com.ecommerce.outbox")
@EntityScan(basePackages = "com.ecommerce.outbox.entities")
public class OutboxAutoConfiguration {
}
