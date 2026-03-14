package com.example.hpayments;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class MicroservicioHpaymentsMainTest {

    @Test
    void main_invokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext ctx = Mockito.mock(ConfigurableApplicationContext.class);
            // Stub the static call for the exact arguments (class and empty String[])
            mocked.when(() -> SpringApplication.run(MicroservicioHpaymentsApplication.class, new String[0]))
                    .thenReturn(ctx);

            // Call the main method with empty args to match the stub
            MicroservicioHpaymentsApplication.main(new String[0]);

            // Verify SpringApplication.run was called
            mocked.verify(() -> SpringApplication.run(MicroservicioHpaymentsApplication.class, new String[0]));
        }
    }
}