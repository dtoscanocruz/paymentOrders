package com.example.hpayments.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditLogEntryTest {

    @Test
    void builder_and_getters_should_work() {
        Instant now = Instant.now();
        AuditLogEntry entry = AuditLogEntry.builder()
                .id(1L)
                .requestId("req-1")
                .path("/api/test")
                .method("GET")
                .username("user1")
                .status(200)
                .errorMessage(null)
                .payload("{\"foo\":\"bar\"}")
                .startedAt(now)
                .endedAt(now.plusMillis(50))
                .elapsedMs(50L)
                .traceId("trace-123")
                .build();

        assertThat(entry.getId()).isEqualTo(1L);
        assertThat(entry.getRequestId()).isEqualTo("req-1");
        assertThat(entry.getPath()).isEqualTo("/api/test");
        assertThat(entry.getMethod()).isEqualTo("GET");
        assertThat(entry.getUsername()).isEqualTo("user1");
        assertThat(entry.getStatus()).isEqualTo(200);
        assertThat(entry.getPayload()).isEqualTo("{\"foo\":\"bar\"}");
        assertThat(entry.getStartedAt()).isEqualTo(now);
        assertThat(entry.getElapsedMs()).isEqualTo(50L);
        assertThat(entry.getTraceId()).isEqualTo("trace-123");
    }

    @Test
    void setters_should_work_and_toString_contains_fields() {
        AuditLogEntry e = new AuditLogEntry();
        e.setId(2L);
        e.setRequestId("r2");
        e.setPath("/x");
        e.setMethod("POST");
        e.setUsername("u2");
        e.setStatus(500);
        e.setErrorMessage("boom");
        e.setPayload("p");
        e.setStartedAt(Instant.EPOCH);
        e.setEndedAt(Instant.EPOCH.plusMillis(1));
        e.setElapsedMs(1L);
        e.setTraceId("t2");

        String s = e.toString();
        assertThat(s).contains("r2").contains("/x").contains("POST").contains("u2").contains("boom");
    }

    @Test
    void equals_and_hashcode_contract() {
        Instant now = Instant.now();
        AuditLogEntry a = AuditLogEntry.builder()
                .id(10L)
                .requestId("same")
                .path("/p")
                .method("GET")
                .username("u")
                .status(200)
                .startedAt(now)
                .endedAt(now)
                .elapsedMs(0L)
                .traceId("t")
                .build();

        AuditLogEntry b = AuditLogEntry.builder()
                .id(10L)
                .requestId("same")
                .path("/p")
                .method("GET")
                .username("u")
                .status(200)
                .startedAt(now)
                .endedAt(now)
                .elapsedMs(0L)
                .traceId("t")
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        AuditLogEntry c = AuditLogEntry.builder().id(11L).requestId("other").build();
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");
    }

    @Test
    void null_fields_and_symmetry() {
        AuditLogEntry a = AuditLogEntry.builder().id(100L).requestId(null).path(null).build();
        AuditLogEntry b = AuditLogEntry.builder().id(100L).requestId(null).path(null).build();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        AuditLogEntry c = AuditLogEntry.builder().id(100L).requestId("x").path(null).build();
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void subclass_vs_parent_equals() {
        class SubEntry extends AuditLogEntry { }
        AuditLogEntry parent = AuditLogEntry.builder().id(200L).requestId("z").build();
        SubEntry sub = new SubEntry();
        sub.setId(200L);
        sub.setRequestId("z");
        // Lombok equals typically compares classes; assert equality behavior is symmetric
        assertThat(parent).isEqualTo(sub);
        assertThat(sub).isEqualTo(parent);
    }
}
