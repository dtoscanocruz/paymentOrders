package com.example.hpayments.application;

import com.example.hpayments.domain.PaymentOrder;
import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.out.PaymentOrderRepositoryPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOrderServiceSmokeTest {

    static class InMemoryRepo implements PaymentOrderRepositoryPort {
        @Override
        public Mono<PaymentOrder> save(PaymentOrder paymentOrder) {
            // simulate persistence by returning the same instance with lastUpdate set
            paymentOrder.setLastUpdate(LocalDateTime.now());
            return Mono.just(paymentOrder);
        }

        @Override
        public Mono<PaymentOrder> findById(String id) {
            if ("e-smoke".equals(id)) {
                PaymentOrder po = PaymentOrder.builder()
                        .externalId("e-smoke")
                        .status("SMOKE")
                        .lastUpdate(LocalDateTime.now())
                        .build();
                return Mono.just(po);
            }
            return Mono.empty();
        }
    }

    @Test
    void smoke_submit_and_getStatus() {
        PaymentOrderRepositoryPort repo = new InMemoryRepo();
        PaymentOrderService svc = new PaymentOrderService(repo);

        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .externalId("smoke-ext")
                .debtorIban("D1")
                .creditorIban("C1")
                .amount(new BigDecimal("5.00"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.now())
                .build();

        StepVerifier.create(svc.submit(req))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("smoke-ext");
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        StepVerifier.create(svc.getStatus("e-smoke"))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("e-smoke");
                    assertThat(resp.getStatus()).isEqualTo("SMOKE");
                })
                .verifyComplete();
    }
}
