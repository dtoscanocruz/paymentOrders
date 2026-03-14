package com.example.hpayments.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_log")
public class AuditLogEntry {

    @Id
    private Long id;

    private String requestId;
    private String path;
    private String method;
    private String username;
    private Integer status;
    private String errorMessage;
    private String payload;
    private Instant startedAt;
    private Instant endedAt;
    private Long elapsedMs;
    private String traceId;

}
