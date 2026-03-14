package com.example.hpayments.ports.dto;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "GetPaymentOrderStatusRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetPaymentOrderStatusRequest {
    private String paymentOrderId;
}
