package com.example.hpayments.adapter.outbound;

import com.example.hpayments.domain.AuditLogEntry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SpringDataAuditLogRepository extends ReactiveCrudRepository<AuditLogEntry, Long> {

    Flux<AuditLogEntry> findByRequestId(String requestId);

}
