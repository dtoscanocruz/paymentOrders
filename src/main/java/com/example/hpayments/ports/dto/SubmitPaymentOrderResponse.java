package com.example.hpayments.ports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitPaymentOrderResponse {
    private String paymentOrderId;
    private String status;
}
