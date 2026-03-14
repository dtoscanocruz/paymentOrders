package com.example.hpayments;

import com.example.hpayments.adapters.rest.PaymentOrderController;
import com.example.hpayments.adapters.rest.mapper.PaymentOrderMapper;
import com.example.hpayments.ports.in.GetPaymentOrderStatusUseCase;
import com.example.hpayments.ports.in.SubmitPaymentOrderUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = PaymentOrderController.class)
public class PaymentOrderControllerSmokeTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private SubmitPaymentOrderUseCase submitUseCase;

    @MockBean
    private GetPaymentOrderStatusUseCase statusUseCase;

    @MockBean
    private PaymentOrderMapper mapper;

    @Test
    void contextLoads() {
        // basic smoke test to ensure controller loads
    }
}
