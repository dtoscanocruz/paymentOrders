package com.example.hpayments.adapters.rest;

import com.example.hpayments.adapters.rest.mapper.PaymentOrderMapper;
import com.example.hpayments.ports.in.GetPaymentOrderStatusUseCase;
import com.example.hpayments.ports.in.SubmitPaymentOrderUseCase;
import com.example.hpayments.rest.adapter.api.PaymentOrderApi;
import com.example.hpayments.rest.adapter.dto.ErrorResponse;
import com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse;
import com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest;
import com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class PaymentOrderController implements PaymentOrderApi {

    private final SubmitPaymentOrderUseCase submitUseCase;
    private final GetPaymentOrderStatusUseCase statusUseCase;
    private final PaymentOrderMapper mapper;

    public PaymentOrderController(SubmitPaymentOrderUseCase submitUseCase, GetPaymentOrderStatusUseCase statusUseCase, PaymentOrderMapper mapper) {
        this.submitUseCase = submitUseCase;
        this.statusUseCase = statusUseCase;
        this.mapper = mapper;
    }

    @Override
    public Mono<ResponseEntity<SubmitPaymentOrderResponse>> submitPaymentOrder(Mono<SubmitPaymentOrderRequest> submitPaymentOrderRequest, final ServerWebExchange exchange) {
        return submitPaymentOrderRequest
                .map(mapper::toPort)
                .flatMap(portReq -> submitUseCase.submit(portReq))
                .map(mapper::toApi)
                .map(resp -> {
                    // Build Location from the incoming request URI so we don't hardcode paths.
                    java.net.URI requestUri = exchange.getRequest().getURI();
                    String base = requestUri.toString();
                    if (!base.endsWith("/")) {
                        base = base + "/";
                    }
                    URI location = URI.create(base + resp.getPaymentOrderId());
                    return ResponseEntity.created(location).body(resp);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    // We intentionally perform an unchecked cast here because the generated interface
    // requires a ResponseEntity<GetPaymentOrderStatusResponse> but in the error case
    // we return an ErrorResponse body with a 404 status. The cast is safe: the HTTP
    // status code indicates an error body, and JSON serialization will produce the
    // correct payload. This keeps compatibility with the generated OpenAPI interface
    // while providing a structured error body to clients.
    public Mono<ResponseEntity<GetPaymentOrderStatusResponse>> getPaymentOrderStatus(String paymentOrderId, final ServerWebExchange exchange) {
        return statusUseCase.getStatus(paymentOrderId)
                .map(mapper::toApi)
                .map(ResponseEntity::ok)
                .defaultIfEmpty((ResponseEntity<GetPaymentOrderStatusResponse>)(ResponseEntity<?>)ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildNotFoundError(paymentOrderId)));
    }

    @Override
    @SuppressWarnings("unchecked")
    // Same rationale as above: when the resource is not found we return an ErrorResponse
    // with HTTP 404. The cast silences the generic type mismatch between
    // ResponseEntity<SubmitPaymentOrderResponse> (expected by the generated API) and
    // ResponseEntity<ErrorResponse> used for the error body.
    public Mono<ResponseEntity<SubmitPaymentOrderResponse>> getPaymentOrder(String paymentOrderId, final ServerWebExchange exchange) {
        return statusUseCase.getStatus(paymentOrderId)
                .map(portResp -> {
                    SubmitPaymentOrderResponse apiResp = new SubmitPaymentOrderResponse();
                    apiResp.setPaymentOrderId(portResp.getPaymentOrderId());
                    apiResp.setStatus(portResp.getStatus());
                    return ResponseEntity.ok(apiResp);
                })
                .defaultIfEmpty((ResponseEntity<SubmitPaymentOrderResponse>)(ResponseEntity<?>)ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildNotFoundError(paymentOrderId)));
    }

    private ErrorResponse buildNotFoundError(String paymentOrderId) {
        ErrorResponse err = new ErrorResponse();
        err.setCode("NOT_FOUND");
        err.setMessage("Payment order not found: " + paymentOrderId);
        return err;
    }

}