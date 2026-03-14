package com.example.hpayments.application;

import com.example.hpayments.domain.PaymentOrder;
import com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse;
import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import com.example.hpayments.ports.out.PaymentOrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock
    PaymentOrderRepositoryPort repository;

    @InjectMocks
    PaymentOrderService service;

    @Test
    void submit_withExternalId_callsSaveAndReturnsResponse() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .externalId("ext-123")
                .debtorIban("DIBAN")
                .creditorIban("CIBAN")
                .amount(new BigDecimal("100"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026, 3, 12))
                .build();

        PaymentOrder saved = PaymentOrder.builder()
                .externalId("ext-123")
                .debtorIban("DIBAN")
                .creditorIban("CIBAN")
                .amount(new BigDecimal("100"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026, 3, 12))
                .status("PENDING")
                .lastUpdate(LocalDateTime.now())
                .build();

        when(repository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(service.submit(req))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("ext-123");
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        ArgumentCaptor<PaymentOrder> capt = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(capt.capture());
        PaymentOrder passed = capt.getValue();
        assertThat(passed.getExternalId()).isEqualTo("ext-123");
        assertThat(passed.getStatus()).isEqualTo("PENDING");
        assertThat(passed.getDebtorIban()).isEqualTo("DIBAN");
    }

    @Test
    void submit_withoutExternalId_generatesExternalId_and_returnsResponse() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .debtorIban("D2")
                .creditorIban("C2")
                .amount(new BigDecimal("1.23"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.of(2026, 3, 13))
                .build();

        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.submit(req))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isNotNull();
                    assertThat(resp.getPaymentOrderId()).isNotEmpty();
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        ArgumentCaptor<PaymentOrder> capt = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(capt.capture());
        PaymentOrder passed = capt.getValue();
        assertThat(passed.getExternalId()).isNotNull();
        assertThat(passed.getExternalId()).isNotEmpty();
    }

    @Test
    void getStatus_found_returnsResponse() {
        PaymentOrder po = PaymentOrder.builder()
                .externalId("e1")
                .status("SETTLED")
                .lastUpdate(LocalDateTime.now())
                .build();

        when(repository.findById("e1")).thenReturn(Mono.just(po));

        StepVerifier.create(service.getStatus("e1"))
                .assertNext((GetPaymentOrderStatusResponse resp) -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("e1");
                    assertThat(resp.getStatus()).isEqualTo("SETTLED");
                    assertThat(resp.getLastUpdate()).isEqualTo(po.getLastUpdate());
                })
                .verifyComplete();

        verify(repository).findById("e1");
    }

    @Test
    void getStatus_notFound_returnsEmpty() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.getStatus("missing"))
                .expectComplete()
                .verify();

        verify(repository).findById("missing");
    }

    @Test
    void submit_withNullAmount_allowsNullAmount() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .externalId("ext-null-amt")
                .debtorIban("DIBAN")
                .creditorIban("CIBAN")
                .amount(null)
                .currency("EUR")
                .requestedExecutionDate(LocalDate.of(2026, 3, 12))
                .build();

        PaymentOrder saved = PaymentOrder.builder()
                .externalId("ext-null-amt")
                .status("PENDING")
                .amount(null)
                .lastUpdate(LocalDateTime.now())
                .build();

        when(repository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(service.submit(req))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("ext-null-amt");
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        ArgumentCaptor<PaymentOrder> capt = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(capt.capture());
        PaymentOrder passed = capt.getValue();
        assertThat(passed.getAmount()).isNull();
    }

    @Test
    void submit_withNullRequestedExecutionDate_allowsNullDate() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .externalId("ext-null-date")
                .debtorIban("DIBAN")
                .creditorIban("CIBAN")
                .amount(new BigDecimal("5.00"))
                .currency("EUR")
                .requestedExecutionDate(null)
                .build();

        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.submit(req))
                .assertNext(resp -> {
                    assertThat(resp.getPaymentOrderId()).isNotNull();
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        ArgumentCaptor<PaymentOrder> capt = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(capt.capture());
        PaymentOrder passed = capt.getValue();
        assertThat(passed.getRequestedExecutionDate()).isNull();
    }

    @Test
    void getStatus_withNullFields_returnsNullsInResponse() {
        PaymentOrder po = PaymentOrder.builder()
                .externalId("e-null")
                .status(null)
                .lastUpdate(null)
                .build();

        when(repository.findById("e-null")).thenReturn(Mono.just(po));

        StepVerifier.create(service.getStatus("e-null"))
                .assertNext((GetPaymentOrderStatusResponse resp) -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("e-null");
                    assertThat(resp.getStatus()).isNull();
                    assertThat(resp.getLastUpdate()).isNull();
                })
                .verifyComplete();

        verify(repository).findById("e-null");
    }
}