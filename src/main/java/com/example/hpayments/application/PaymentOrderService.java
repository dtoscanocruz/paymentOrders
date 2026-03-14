package com.example.hpayments.application;

import com.example.hpayments.domain.PaymentOrder;
import com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse;
import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import com.example.hpayments.ports.in.GetPaymentOrderStatusUseCase;
import com.example.hpayments.ports.in.SubmitPaymentOrderUseCase;
import com.example.hpayments.ports.out.PaymentOrderRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentOrderService implements SubmitPaymentOrderUseCase, GetPaymentOrderStatusUseCase {

    private final PaymentOrderRepositoryPort repository;

    public PaymentOrderService(PaymentOrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<SubmitPaymentOrderResponse> submit(SubmitPaymentOrderRequest request) {
        PaymentOrder po = PaymentOrder.builder()
                .externalId(request.getExternalId() != null ? request.getExternalId() : UUID.randomUUID().toString())
                .debtorIban(request.getDebtorIban())
                .creditorIban(request.getCreditorIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .remittanceInfo(request.getRemittanceInfo())
                .requestedExecutionDate(request.getRequestedExecutionDate())
                .status("PENDING")
                .lastUpdate(LocalDateTime.now())
                .build();

        return repository.save(po)
                .map(saved -> SubmitPaymentOrderResponse.builder()
                        .paymentOrderId(saved.getExternalId())
                        .status(saved.getStatus())
                        .build());
    }

    @Override
    public Mono<GetPaymentOrderStatusResponse> getStatus(String paymentOrderId) {
        return repository.findById(paymentOrderId)
                .map(po -> GetPaymentOrderStatusResponse.builder()
                        .paymentOrderId(po.getExternalId())
                        .status(po.getStatus())
                        .lastUpdate(po.getLastUpdate())
                        .build());
    }
}
