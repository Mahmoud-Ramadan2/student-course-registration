package com.mahmoudramadan.studentregistration.infra.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
public class DatabaseHealthIndicator extends AbstractHealthIndicator {
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                builder.up()
                        .withDetail("database", conn.getMetaData().getDatabaseProductName())
                        .withDetail("url", conn.getMetaData().getURL());
            } else {
                log.warn("Database health check - unreachable");
                builder.down().withDetail("database", "unreachable");
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            builder.down().withDetail("error", e.getMessage());
        }
    }
}
