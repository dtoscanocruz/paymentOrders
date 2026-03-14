package com.example.hpayments.adapters.rest;

import com.example.hpayments.adapters.rest.mapper.PaymentOrderMapper;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import com.example.hpayments.ports.in.GetPaymentOrderStatusUseCase;
import com.example.hpayments.ports.in.SubmitPaymentOrderUseCase;
import com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@WebFluxTest(controllers = PaymentOrderController.class)
public class PaymentOrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SubmitPaymentOrderUseCase submitUseCase;

    @MockBean
    private GetPaymentOrderStatusUseCase statusUseCase;

    @MockBean
    private PaymentOrderMapper mapper;

    private SubmitPaymentOrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new SubmitPaymentOrderRequest();
        sampleRequest.setExternalId("ext-1");
        sampleRequest.setDebtorIban("DE123");
        sampleRequest.setCreditorIban("ES123");
        sampleRequest.setAmount(100.00); // use Double as generated DTO expects
        sampleRequest.setCurrency("EUR");
        sampleRequest.setRequestedExecutionDate(LocalDate.now());
    }

    @Test
    void submitPaymentOrder_returnsCreated() {
        com.example.hpayments.ports.dto.SubmitPaymentOrderRequest portReq = com.example.hpayments.ports.dto.SubmitPaymentOrderRequest.builder()
                .debtorIban("DE123")
                .creditorIban("ES123")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .requestedExecutionDate(LocalDate.now())
                .build();

        com.example.hpayments.ports.dto.SubmitPaymentOrderResponse portResp = new com.example.hpayments.ports.dto.SubmitPaymentOrderResponse();
        portResp.setPaymentOrderId("PO123");
        portResp.setStatus("PENDING");

        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse apiResp = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse();
        apiResp.setPaymentOrderId("PO123");
        apiResp.setStatus("PENDING");

        Mockito.when(mapper.toPort(Mockito.any( com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest.class))).thenReturn(portReq);
        Mockito.when(submitUseCase.submit(Mockito.any())).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.any(com.example.hpayments.ports.dto.SubmitPaymentOrderResponse.class))).thenReturn(apiResp);

        webTestClient.post()
                .uri("/payment-initiation/payment-orders")
                .bodyValue(sampleRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/PO123$")
                .expectBody()
                .jsonPath("$.paymentOrderId").isEqualTo("PO123")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void getPaymentOrderStatus_returnsOk() {
        com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse portResp = new com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse();
        portResp.setPaymentOrderId("PO123");
        portResp.setStatus("ACCEPTED");
        portResp.setLastUpdate(java.time.LocalDateTime.now());

        com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse apiResp = new com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse();
        apiResp.setPaymentOrderId("PO123");
        apiResp.setStatus("ACCEPTED");
        apiResp.setLastUpdate(java.time.OffsetDateTime.now());

        Mockito.when(statusUseCase.getStatus(Mockito.eq("PO123"))).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.any(com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse.class))).thenReturn(apiResp);

        webTestClient.get()
                .uri("/payment-initiation/payment-orders/PO123/status")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paymentOrderId").isEqualTo("PO123")
                .jsonPath("$.status").isEqualTo("ACCEPTED");
    }
}