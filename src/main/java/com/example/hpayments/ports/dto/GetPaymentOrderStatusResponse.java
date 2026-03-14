package com.example.hpayments.ports.dto;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "GetPaymentOrderStatusResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetPaymentOrderStatusResponse {
    private String paymentOrderId;
    private String status;
    @XmlJavaTypeAdapter(com.example.hpayments.contract.adapters.LocalDateTimeAdapter.class)
    private LocalDateTime lastUpdate;
}