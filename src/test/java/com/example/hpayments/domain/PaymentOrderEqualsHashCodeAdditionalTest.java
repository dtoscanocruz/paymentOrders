package com.example.hpayments.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOrderEqualsHashCodeAdditionalTest {

    PaymentOrder base() {
        return PaymentOrder.builder()
                .id(1L)
                .externalId("ext-1")
                .debtorIban("DIBAN")
                .creditorIban("CIBAN")
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .remittanceInfo("info")
                .requestedExecutionDate(LocalDate.now())
                .status("PENDING")
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    @Test
    void equals_sameObject_isTrue() {
        PaymentOrder a = base();
        assertThat(a.equals(a)).isTrue();
    }

    @Test
    void equals_null_isFalse() {
        PaymentOrder a = base();
        assertThat(a.equals(null)).isFalse();
    }

    @Test
    void equals_differentType_isFalse() {
        PaymentOrder a = base();
        assertThat(a.equals("not-a-payment-order")).isFalse();
    }

    @Test
    void equals_allFieldsEqual_isTrue() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(a);
    }

    @Test
    void equals_differentExternalId_isFalse() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        b.setExternalId("ext-other");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_differentAmount_isFalse() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        b.setAmount(new BigDecimal("11.00"));
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_withNullFields_equalWhenBothNull() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        a.setRemittanceInfo(null);
        b.setRemittanceInfo(null);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_symmetry_property() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        a.setStatus("DONE");
        b.setStatus("DONE");
        assertThat(a.equals(b)).isEqualTo(b.equals(a));
    }

    @Test
    void equals_transitivity_property() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        PaymentOrder c = base();
        // same external id ensures equality across
        a.setExternalId("t1");
        b.setExternalId("t1");
        c.setExternalId("t1");

        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(c);
        assertThat(a).isEqualTo(c);
    }

    @Test
    void hashCode_equalObjects_sameHash() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void hashCode_differentExternalId_differentHashLikely() {
        PaymentOrder a = base();
        PaymentOrder b = base();
        b.setExternalId("different-ext");
        // not strictly required by spec but likely different; at least ensure not all equal
        assertThat(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }
}
