package com.example.hpayments.ports.out;

import com.example.hpayments.domain.PaymentOrder;
import reactor.core.publisher.Mono;

public interface PaymentOrderRepositoryPort {
    Mono<PaymentOrder> save(PaymentOrder paymentOrder);
    Mono<PaymentOrder> findById(String paymentOrderId);
}
