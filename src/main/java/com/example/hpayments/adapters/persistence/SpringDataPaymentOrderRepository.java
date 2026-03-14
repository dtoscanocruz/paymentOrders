package com.example.hpayments.adapters.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface SpringDataPaymentOrderRepository extends ReactiveCrudRepository<SpringDataPaymentOrderEntity, Long> {
    Mono<SpringDataPaymentOrderEntity> findByExternalId(String externalId);
}
