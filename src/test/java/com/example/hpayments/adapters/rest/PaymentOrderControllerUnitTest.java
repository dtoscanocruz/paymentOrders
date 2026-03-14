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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentOrderControllerUnitTest {

    private SubmitPaymentOrderUseCase submitUseCase;
    private GetPaymentOrderStatusUseCase statusUseCase;
    private PaymentOrderMapper mapper;
    private PaymentOrderController controller;

    @BeforeEach
    void setup() {
        submitUseCase = Mockito.mock(SubmitPaymentOrderUseCase.class);
        statusUseCase = Mockito.mock(GetPaymentOrderStatusUseCase.class);
        mapper = Mockito.mock(PaymentOrderMapper.class);
        controller = new PaymentOrderController(submitUseCase, statusUseCase, mapper);
    }

    @Test
    void submitPaymentOrder_success_unit() {
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest apiReq = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest();
        apiReq.setExternalId("ext-1");

        SubmitPaymentOrderRequest portReq = SubmitPaymentOrderRequest.builder().externalId("ext-1").amount(new BigDecimal("1.00")).build();
        SubmitPaymentOrderResponse portResp = new SubmitPaymentOrderResponse("po-1","PENDING");
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse apiResp = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse();
        apiResp.setPaymentOrderId("po-1"); apiResp.setStatus("PENDING");

        Mockito.when(mapper.toPort(Mockito.any())).thenReturn(portReq);
        Mockito.when(submitUseCase.submit(Mockito.eq(portReq))).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.eq(portResp))).thenReturn(apiResp);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("http://localhost/payment-initiation/payment-orders"));

        var respMono = controller.submitPaymentOrder(Mono.just(apiReq), exchange);
        var resp = respMono.block();

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getHeaders().getLocation().toString()).endsWith("/po-1");
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo("po-1");
        // Additional assertions: ensure the Location path ends with the id and does not contain a double slash
        URI locationUri = resp.getHeaders().getLocation();
        String locationPath = locationUri.getPath();
        assertThat(locationPath).endsWith("/po-1");
        assertThat(locationPath).doesNotContain("//");
    }

    @Test
    void getPaymentOrderStatus_found_unit() {
        String id = "po-1";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder().paymentOrderId(id).status("COMPLETED").lastUpdate(LocalDateTime.of(2023,1,1,12,0)).build();
        com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse apiResp = new com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse();
        apiResp.setPaymentOrderId(id); apiResp.setStatus("COMPLETED"); apiResp.setLastUpdate(OffsetDateTime.of(2023,1,1,12,0,0,0, ZoneOffset.UTC));

        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.eq(portResp))).thenReturn(apiResp);

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id+"/status"));

        var resp = controller.getPaymentOrderStatus(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo(id);
    }

    @Test
    void getPaymentOrderStatus_notFound_unit() {
        String id = "po-404";
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id+"/status"));

        var resp = controller.getPaymentOrderStatus(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        Object body = resp.getBody();
        assertThat(body).isInstanceOf(com.example.hpayments.rest.adapter.dto.ErrorResponse.class);
        assertThat(((com.example.hpayments.rest.adapter.dto.ErrorResponse)body).getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void getPaymentOrder_found_unit() {
        String id = "po-1";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder().paymentOrderId(id).status("COMPLETED").lastUpdate(LocalDateTime.of(2023,1,1,12,0)).build();
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id));

        var resp = controller.getPaymentOrder(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo(id);
    }

    @Test
    void getPaymentOrder_notFound_unit() {
        String id = "po-404";
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id));

        var resp = controller.getPaymentOrder(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        Object body = resp.getBody();
        assertThat(body).isInstanceOf(com.example.hpayments.rest.adapter.dto.ErrorResponse.class);
        assertThat(((com.example.hpayments.rest.adapter.dto.ErrorResponse)body).getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void getPaymentOrder_variedStatus_unit() {
        String id = "po-2";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId(id)
                .status("FAILED")
                .lastUpdate(LocalDateTime.of(2023,2,2,10,0))
                .build();
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id));

        var resp = controller.getPaymentOrder(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo(id);
        assertThat(resp.getBody().getStatus()).isEqualTo("FAILED");
    }

    @Test
    void getPaymentOrder_nullStatus_unit() {
        String id = "po-3";
        GetPaymentOrderStatusResponse portResp = GetPaymentOrderStatusResponse.builder()
                .paymentOrderId(id)
                .status(null)
                .lastUpdate(LocalDateTime.of(2023,3,3,9,0))
                .build();
        Mockito.when(statusUseCase.getStatus(id)).thenReturn(Mono.just(portResp));

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/payment-initiation/payment-orders/"+id));

        var resp = controller.getPaymentOrder(id, exchange).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo(id);
        // The API response status may be null if the port status is null
        assertThat(resp.getBody().getStatus()).isNull();
    }

    @Test
    void submitPaymentOrder_locationEndsWithSlash_unit() {
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest apiReq = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest();
        apiReq.setExternalId("ext-1");
        apiReq.setDebtorIban("DE89370400440532013000");
        apiReq.setCreditorIban("FR7630006000011234567890189");
        apiReq.setAmount(10.0);
        apiReq.setCurrency("EUR");
        apiReq.setRequestedExecutionDate(java.time.LocalDate.of(2023,1,1));

        SubmitPaymentOrderRequest portReq = SubmitPaymentOrderRequest.builder().externalId("ext-1").amount(new BigDecimal("10.00")).build();
        SubmitPaymentOrderResponse portResp = new SubmitPaymentOrderResponse("po-1","PENDING");
        com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse apiResp = new com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse();
        apiResp.setPaymentOrderId("po-1"); apiResp.setStatus("PENDING");

        Mockito.when(mapper.toPort(Mockito.any())).thenReturn(portReq);
        Mockito.when(submitUseCase.submit(Mockito.eq(portReq))).thenReturn(Mono.just(portResp));
        Mockito.when(mapper.toApi(Mockito.eq(portResp))).thenReturn(apiResp);

        // Request URI already ends with '/'
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("http://localhost/payment-initiation/payment-orders/").header("Host", "localhost"));

        var respMono = controller.submitPaymentOrder(Mono.just(apiReq), exchange);
        var resp = respMono.block();

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        // Location should be base + id without duplicating slashes
        assertThat(resp.getHeaders().getLocation().toString()).endsWith("/po-1");
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getPaymentOrderId()).isEqualTo("po-1");
        // Additional assertions: ensure the Location path ends with the id and does not contain a double slash
        URI locationUri2 = resp.getHeaders().getLocation();
        String locationPath2 = locationUri2.getPath();
        assertThat(locationPath2).endsWith("/po-1");
        assertThat(locationPath2).doesNotContain("//");
    }

}