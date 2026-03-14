package com.example.hpayments.ports.in;

import com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse;
import reactor.core.publisher.Mono;

public interface GetPaymentOrderStatusUseCase {
    Mono<GetPaymentOrderStatusResponse> getStatus(String paymentOrderId);
}
