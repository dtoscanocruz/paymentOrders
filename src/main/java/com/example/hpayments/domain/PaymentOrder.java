package com.example.hpayments.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_orders")
public class PaymentOrder {
    @Id
    private Long id;
    private String externalId;
    private String debtorIban;
    private String creditorIban;
    private BigDecimal amount;
    private String currency;
    private String remittanceInfo;
    private LocalDate requestedExecutionDate;
    private String status;
    private LocalDateTime lastUpdate;
}
