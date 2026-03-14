package com.example.hpayments;

import com.example.hpayments.adapter.outbound.SpringDataAuditLogRepository;
import com.example.hpayments.domain.AuditLogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Instant;

@DataR2dbcTest
@Import(com.example.hpayments.config.R2dbcConfiguration.class)
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
public class AuditLogRepositoryIntegrationTest {

    @Autowired
    private SpringDataAuditLogRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void saveAndFindByRequestId() {
        // Create table in H2 (compatible SQL)
        databaseClient.sql("CREATE TABLE IF NOT EXISTS audit_log (id BIGINT PRIMARY KEY AUTO_INCREMENT, request_id VARCHAR(128) NOT NULL, path VARCHAR(1024), method VARCHAR(16), username VARCHAR(255), status INT, error_message VARCHAR(2048), payload CLOB, started_at TIMESTAMP NULL, ended_at TIMESTAMP NULL, elapsed_ms BIGINT, trace_id VARCHAR(128), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
                .fetch()
                .rowsUpdated()
                .block();

        String reqId = "test-req-1";
        AuditLogEntry entry = AuditLogEntry.builder()
                .requestId(reqId)
                .path("/api/test")
                .method("GET")
                .username("user1")
                .status(200)
                .payload("{}")
                .startedAt(Instant.now())
                .endedAt(Instant.now())
                .elapsedMs(10L)
                .traceId("trace-1")
                .build();

        StepVerifier.create(repository.save(entry))
                .expectNextMatches(saved -> saved.getId() != null && saved.getRequestId().equals(reqId))
                .verifyComplete();

        StepVerifier.create(repository.findByRequestId(reqId).collectList())
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getRequestId().equals(reqId))
                .verifyComplete();
    }
}