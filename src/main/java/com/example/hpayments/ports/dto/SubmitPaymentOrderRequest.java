package com.example.hpayments.ports.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "SubmitPaymentOrderRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmitPaymentOrderRequest {
    private String externalId;
    private String debtorIban;
    private String creditorIban;
    private BigDecimal amount;
    private String currency;
    private String remittanceInfo;
    @XmlJavaTypeAdapter(com.example.hpayments.contract.adapters.LocalDateAdapter.class)
    private LocalDate requestedExecutionDate;
}