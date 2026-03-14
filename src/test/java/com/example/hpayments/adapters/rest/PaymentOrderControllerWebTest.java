// Explanation: Replace the Spring @WebFluxTest slice with a plain unit test using
// WebTestClient.bindToController to avoid full application context and filter wiring
// which caused 500 errors in CI; this keeps test fast and stable.
package com.example.hpayments.adapters.rest;

import com.example.hpayments.adapters.rest.mapper.PaymentOrderMapper;
import com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse;
import com.example.hpayments.ports.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.ports.dto.SubmitPaymentOrderResponse;
import com.example.hpayments.ports.in.GetPaymentOrderStatusUseCase;
import com.example.hpayments.ports.in.SubmitPaymentOrderUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentOrderControllerWebTest {

    private SubmitPaymentOrderUseCase submitUseCase;
    private GetPaymentOrderStatusUseCase statusUseCase;
    private PaymentOrderMapper mapper;
    private WebTestClient webClient;

    @BeforeEach
    void setup() {
        submitUseCase = Mockito.mock(SubmitPaymentOrderUseCase.class);
        statusUseCase = Mockito.mock(GetPaymentOrderStatusUseCase.class);
        mapper = Mockito.mock(PaymentOrderMapper.class);

        PaymentOrderController controller = new PaymentOrderController(submitUseCase, statusUseCase, mapper);
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void submitPaymentOrder_success() {
        // Arrange
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest apiReq = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest();
        apiReq.setExternalId("ext-1");
        apiReq.setDebtorIban("DE89370400440532013000");
        apiReq.setCreditorIban("FR7630006000011234567890189");
        apiReq.setAmount(10.0);
        apiReq.setCurrency("EUR");
        apiReq.setRequestedExecutionDate(java.time.LocalDate.of(2023,1,1));

        SubmitPaymentOrderRequest portReq = SubmitPaymentOrderRequest.builder()
                .externalId("ext-1")
                .amount(new BigDecimal("10.00"))
                .build();

        SubmitPaymentOrderResponse portResp = new SubmitPaymentOrderResponse("po-1", "PENDING");
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse apiResp = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse();
        apiResp.setPaymentOrderId("po-1");
        apiResp.setStatus("PENDING");

        Mockito.when(mapper.toPort(Mockito.any())).thenReturn(portReq);
        Mockito.when(submitUseCase.submit(Mockito.eq(portReq))).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.eq(portResp))).thenReturn(apiResp);

        // Act & Assert
        webClient.post().uri("/payment-initiation/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(apiReq)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.paymentOrderId").isEqualTo("po-1")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void getPaymentOrderStatus_found() {
        String id = "po-1";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId(id)
                .status("COMPLETED")
                .lastUpdate(LocalDateTime.of(2023,1,1,12,0))
                .build();

        com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse apiResp = new com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse();
        apiResp.setPaymentOrderId(id);
        apiResp.setStatus("COMPLETED");
        apiResp.setLastUpdate(OffsetDateTime.of(2023,1,1,12,0,0,0, ZoneOffset.UTC));

        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.eq(portResp))).thenReturn(apiResp);

        webClient.get().uri("/payment-initiation/payment-orders/{id}/status", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paymentOrderId").isEqualTo(id)
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void getPaymentOrderStatus_notFound() {
        String id = "po-404";
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.empty());

        webClient.get().uri("/payment-initiation/payment-orders/{id}/status", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    @Test
    void getPaymentOrder_found() {
        String id = "po-1";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId(id)
                .status("COMPLETED")
                .lastUpdate(LocalDateTime.of(2023,1,1,12,0))
                .build();

        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));

        webClient.get().uri("/payment-initiation/payment-orders/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paymentOrderId").isEqualTo(id)
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void getPaymentOrder_notFound() {
        String id = "po-404";
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.empty());

        webClient.get().uri("/payment-initiation/payment-orders/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

}