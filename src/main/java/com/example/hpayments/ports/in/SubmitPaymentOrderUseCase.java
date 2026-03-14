package com.example.hpayments.ports.in;

import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import reactor.core.publisher.Mono;

public interface SubmitPaymentOrderUseCase {
    Mono<SubmitPaymentOrderResponse> submit(SubmitPaymentOrderRequest request);
}
