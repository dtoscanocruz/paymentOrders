package com.example.hpayments.application;

import com.example.hpayments.domain.PaymentOrder;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceAdditionalTest {

    @Mock
    PaymentOrderRepositoryPort repository;

    @InjectMocks
    PaymentOrderService service;

    @Test
    void submit_repositoryError_propagatesError() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .debtorIban("DERR")
                .creditorIban("CERR")
                .amount(new BigDecimal("10"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.now())
                .build();

        when(repository.save(any())).thenReturn(Mono.error(new RuntimeException("DB fail")));

        StepVerifier.create(service.submit(req))
                .expectErrorMessage("DB fail")
                .verify();

        verify(repository).save(any());
    }

    @Test
    void getStatus_repositoryError_propagatesError() {
        when(repository.findById("id-err")).thenReturn(Mono.error(new IllegalStateException("boom")));

        StepVerifier.create(service.getStatus("id-err"))
                .expectErrorMessage("boom")
                .verify();

        verify(repository).findById("id-err");
    }

    @Test
    void submit_savedHasDifferentStatus_mapsCorrectly() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .externalId("ext-9")
                .debtorIban("D9")
                .creditorIban("C9")
                .amount(new BigDecimal("99"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.now())
                .build();

        PaymentOrder saved = PaymentOrder.builder()
                .externalId("ext-9")
                .status("SETTLED")
                .lastUpdate(LocalDateTime.now())
                .build();

        when(repository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(service.submit(req))
                .assertNext((SubmitPaymentOrderResponse resp) -> {
                    assertThat(resp.getPaymentOrderId()).isEqualTo("ext-9");
                    assertThat(resp.getStatus()).isEqualTo("SETTLED");
                })
                .verifyComplete();

        verify(repository).save(any());
    }

    @Test
    void submit_generatedExternalId_isUUID_and_passedToRepository() {
        SubmitPaymentOrderRequest req = SubmitPaymentOrderRequest.builder()
                .debtorIban("D2")
                .creditorIban("C2")
                .amount(new BigDecimal("1.23"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.now())
                .build();

        // Return the same instance saved
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.submit(req))
                .assertNext(resp -> {
                    // ensure returned id is a valid UUID
                    assertThatCode(() -> UUID.fromString(resp.getPaymentOrderId())).doesNotThrowAnyException();
                    assertThat(resp.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();

        ArgumentCaptor<PaymentOrder> capt = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(capt.capture());
        PaymentOrder passed = capt.getValue();
        assertThat(passed.getExternalId()).isNotNull();
        assertThatCode(() -> UUID.fromString(passed.getExternalId())).doesNotThrowAnyException();
        assertThat(passed.getStatus()).isEqualTo("PENDING");
    }

}
