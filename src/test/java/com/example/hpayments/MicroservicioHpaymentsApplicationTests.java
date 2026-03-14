package com.example.hpayments;

import com.example.hpayments.adapters.rest.PaymentOrderController;
import com.example.hpayments.adapters.rest.mapper.PaymentOrderMapper;
import com.example.hpayments.application.PaymentOrderService;
import com.example.hpayments.ports.out.PaymentOrderRepositoryPort;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MicroservicioHpaymentsApplicationTests {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private Environment env;

    @MockBean
    private PaymentOrderRepositoryPort repository;

    @MockBean
    private PaymentOrderMapper mapper;

    @Test
    void contextLoads() {
        assertThat(ctx).isNotNull();
    }

    @Test
    void paymentOrderControllerBeanPresent() {
        assertThat(ctx.getBeanNamesForType(PaymentOrderController.class)).isNotEmpty();
    }

    @Test
    void additionalBeansAndPropertiesPresent() {
        // PaymentOrderService should be available as it's annotated with @Service
        assertThat(ctx.getBeanNamesForType(PaymentOrderService.class)).isNotEmpty();

        // ConnectionFactory bean should be present from R2dbcConfiguration
        assertThat(ctx.getBeanNamesForType(ConnectionFactory.class)).isNotEmpty();

        // Check that spring.r2dbc.url property is defined (non-empty)
        String r2dbcUrl = env.getProperty("spring.r2dbc.url");
        assertThat(r2dbcUrl).isNotNull();
        assertThat(r2dbcUrl).isNotEmpty();
        assertThat(r2dbcUrl).contains("r2dbc");
    }
}