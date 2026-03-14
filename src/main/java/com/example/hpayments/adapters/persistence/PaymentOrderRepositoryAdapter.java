package com.example.hpayments.adapters.persistence;

import com.example.hpayments.domain.PaymentOrder;
import com.example.hpayments.ports.out.PaymentOrderRepositoryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class PaymentOrderRepositoryAdapter implements PaymentOrderRepositoryPort {

    private final SpringDataPaymentOrderRepository repository;

    public PaymentOrderRepositoryAdapter(SpringDataPaymentOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PaymentOrder> save(PaymentOrder paymentOrder) {
        // map domain to entity
        SpringDataPaymentOrderEntity entity = SpringDataPaymentOrderEntity.builder()
                .id(paymentOrder.getId())
                .externalId(paymentOrder.getExternalId())
                .debtorIban(paymentOrder.getDebtorIban())
                .creditorIban(paymentOrder.getCreditorIban())
                .amount(paymentOrder.getAmount())
                .currency(paymentOrder.getCurrency())
                .remittanceInfo(paymentOrder.getRemittanceInfo())
                .requestedExecutionDate(paymentOrder.getRequestedExecutionDate())
                .status(paymentOrder.getStatus())
                .lastUpdate(LocalDateTime.now())
                .build();

        return repository.save(entity)
                .map(e -> {
                    paymentOrder.setId(e.getId());
                    paymentOrder.setLastUpdate(e.getLastUpdate());
                    return paymentOrder;
                });
    }

    @Override
    public Mono<PaymentOrder> findById(String paymentOrderId) {
        return repository.findByExternalId(paymentOrderId)
                .map(e -> PaymentOrder.builder()
                        .id(e.getId())
                        .externalId(e.getExternalId())
                        .debtorIban(e.getDebtorIban())
                        .creditorIban(e.getCreditorIban())
                        .amount(e.getAmount())
                        .currency(e.getCurrency())
                        .remittanceInfo(e.getRemittanceInfo())
                        .requestedExecutionDate(e.getRequestedExecutionDate())
                        .status(e.getStatus())
                        .lastUpdate(e.getLastUpdate())
                        .build());
    }
}
