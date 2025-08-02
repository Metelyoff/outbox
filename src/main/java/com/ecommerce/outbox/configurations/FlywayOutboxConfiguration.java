package com.ecommerce.outbox.configurations;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBooleanProperty("spring.application.outbox.flyway.enabled")
public class FlywayOutboxConfiguration {

    @Bean(name = "outboxFlyway")
    public Flyway outboxFlyway(OutboxFlywayProperties props) {
        FluentConfiguration config = Flyway.configure()
                .dataSource(props.getUrl(), props.getUser(), props.getPassword())
                .locations(props.getLocations().split(","))
                .defaultSchema(props.getDefaultSchema())
                .failOnMissingLocations(props.isFailOnMissingLocations());

        if (props.getPlaceholders() != null) {
            config.placeholders(props.getPlaceholders());
        }

        config.baselineOnMigrate(props.isBaselineOnMigrate());

        return config.load();
    }

    @Bean
    public FlywayMigrationInitializer outboxFlywayInitializer(@Qualifier("outboxFlyway") Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }

}
