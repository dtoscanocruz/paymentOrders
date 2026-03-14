package com.example.hpayments.adapters.rest.mapper;

import com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse;
import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOrderMapperTest {

    private final PaymentOrderMapper mapper = Mappers.getMapper(PaymentOrderMapper.class);

    @Test
    void toOffsetDateTime_nullReturnsNull() {
        assertThat(mapper.toOffsetDateTime(null)).isNull();
    }

    @Test
    void toOffsetDateTime_convertsToUtcOffset() {
        LocalDateTime ldt = LocalDateTime.of(2020, 1, 1, 12, 0);
        OffsetDateTime odt = mapper.toOffsetDateTime(ldt);
        assertThat(odt).isEqualTo(ldt.atOffset(ZoneOffset.UTC));
    }

    @Test
    void toPort_mapsSubmitRequestFieldsAndAmount() {
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest api = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest();
        api.setExternalId("ext-1");
        api.setDebtorIban("DE89370400440532013000");
        api.setCreditorIban("FR7630006000011234567890189");
        api.setAmount(5.00);
        api.setCurrency("EUR");
        api.setRemittanceInfo("Invoice 123");
        api.setRequestedExecutionDate(LocalDate.of(2026, 3, 13));

        SubmitPaymentOrderRequest port = mapper.toPort(api);

        assertThat(port).isNotNull();
        assertThat(port.getExternalId()).isEqualTo("ext-1");
        assertThat(port.getDebtorIban()).isEqualTo("DE89370400440532013000");
        assertThat(port.getCreditorIban()).isEqualTo("FR7630006000011234567890189");
        assertThat(port.getAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(port.getCurrency()).isEqualTo("EUR");
        assertThat(port.getRemittanceInfo()).isEqualTo("Invoice 123");
        assertThat(port.getRequestedExecutionDate()).isEqualTo(LocalDate.of(2026, 3, 13));
    }

    @Test
    void toApi_mapsSubmitResponse() {
        SubmitPaymentOrderResponse port = SubmitPaymentOrderResponse.builder()
                .paymentOrderId("p-1")
                .status("PENDING")
                .build();

        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse api = mapper.toApi(port);

        assertThat(api).isNotNull();
        assertThat(api.getPaymentOrderId()).isEqualTo("p-1");
        assertThat(api.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void toApi_getStatus_includesLastUpdateAsOffset() {
        LocalDateTime ldt = LocalDateTime.of(2026, 3, 13, 10, 30, 0);
        GetPaymentOrderStatusResponse port = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId("p-2")
                .status("DONE")
                .lastUpdate(ldt)
                .build();

        com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse api = mapper.toApi(port);

        assertThat(api).isNotNull();
        assertThat(api.getPaymentOrderId()).isEqualTo("p-2");
        assertThat(api.getStatus()).isEqualTo("DONE");
        assertThat(api.getLastUpdate()).isEqualTo(ldt.atOffset(ZoneOffset.UTC));
    }

    // Negative tests: ensure mapper handles nulls gracefully
    @Test
    void toPort_withNullFields_handlesNulls() {
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest api = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest();
        // leave all fields null

        SubmitPaymentOrderRequest port = mapper.toPort(api);

        assertThat(port).isNotNull();
        assertThat(port.getExternalId()).isNull();
        assertThat(port.getDebtorIban()).isNull();
        assertThat(port.getCreditorIban()).isNull();
        assertThat(port.getAmount()).isNull();
        assertThat(port.getCurrency()).isNull();
        assertThat(port.getRemittanceInfo()).isNull();
        assertThat(port.getRequestedExecutionDate()).isNull();
    }

    @Test
    void toApi_submitResponseWithNulls_handlesNulls() {
        SubmitPaymentOrderResponse port = new SubmitPaymentOrderResponse(); // all null

        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse api = mapper.toApi(port);

        assertThat(api).isNotNull();
        assertThat(api.getPaymentOrderId()).isNull();
        assertThat(api.getStatus()).isNull();
    }

    @Test
    void toApi_getStatusWithNullLastUpdate_handlesNull() {
        GetPaymentOrderStatusResponse port = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId("p-3")
                .status("UNKNOWN")
                .lastUpdate(null)
                .build();

        com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse api = mapper.toApi(port);

        assertThat(api).isNotNull();
        assertThat(api.getPaymentOrderId()).isEqualTo("p-3");
        assertThat(api.getStatus()).isEqualTo("UNKNOWN");
        assertThat(api.getLastUpdate()).isNull();
    }
}
