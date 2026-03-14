package com.example.hpayments.adapters.persistence;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SpringDataPaymentOrderEntityTest {

    @Test
    void equalsAndHashCode_sameValues() {
        SpringDataPaymentOrderEntity a = SpringDataPaymentOrderEntity.builder()
                .id(1L)
                .externalId("ext-1")
                .debtorIban("D1")
                .creditorIban("C1")
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026,3,20))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2026,3,20,10,0))
                .build();

        SpringDataPaymentOrderEntity b = SpringDataPaymentOrderEntity.builder()
                .id(1L)
                .externalId("ext-1")
                .debtorIban("D1")
                .creditorIban("C1")
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026,3,20))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2026,3,20,10,0))
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void notEquals_differentField() {
        SpringDataPaymentOrderEntity a = SpringDataPaymentOrderEntity.builder().id(1L).externalId("ext-1").build();
        SpringDataPaymentOrderEntity b = SpringDataPaymentOrderEntity.builder().id(2L).externalId("ext-1").build();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_handlesNullAndDifferentClass() {
        SpringDataPaymentOrderEntity a = SpringDataPaymentOrderEntity.builder().id(1L).build();

        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("some string");
    }

    @Test
    void toString_containsFields() {
        SpringDataPaymentOrderEntity a = SpringDataPaymentOrderEntity.builder()
                .id(9L)
                .externalId("ext-9")
                .status("NEW")
                .build();

        String s = a.toString();
        assertThat(s).contains("ext-9");
        assertThat(s).contains("NEW");
        assertThat(s).contains("9");
    }

    @Test
    void equals_withNullFields() {
        SpringDataPaymentOrderEntity a = SpringDataPaymentOrderEntity.builder()
                .id(null)
                .externalId(null)
                .build();

        SpringDataPaymentOrderEntity b = SpringDataPaymentOrderEntity.builder()
                .id(null)
                .externalId(null)
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
