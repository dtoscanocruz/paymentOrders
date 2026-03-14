package com.example.hpayments.config;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class R2dbcConfigurationTest {

    @Test
    void reactiveTransactionManagerCreatesR2dbcTransactionManager() {
        R2dbcConfiguration cfg = new R2dbcConfiguration();
        ConnectionFactory cf = mock(ConnectionFactory.class);

        ReactiveTransactionManager tx = cfg.reactiveTransactionManager(cf);

        assertNotNull(tx);
        assertTrue(tx instanceof R2dbcTransactionManager);
    }

    @Test
    void connectionFactoryThrowsWhenUrlEmpty() throws Exception {
        R2dbcConfiguration cfg = new R2dbcConfiguration();
        // set private field r2dbcUrl to empty string
        Field f = R2dbcConfiguration.class.getDeclaredField("r2dbcUrl");
        f.setAccessible(true);
        f.set(cfg, "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, cfg::connectionFactory);
        assertTrue(ex.getMessage().toLowerCase().contains("missing configuration"));
    }

    @Test
    void connectionFactoryThrowsWhenUrlInvalid() throws Exception {
        R2dbcConfiguration cfg = new R2dbcConfiguration();
        Field f = R2dbcConfiguration.class.getDeclaredField("r2dbcUrl");
        f.setAccessible(true);
        f.set(cfg, "r2dbc:invalid://localhost");

        IllegalStateException ex = assertThrows(IllegalStateException.class, cfg::connectionFactory);
        assertTrue(ex.getMessage().toLowerCase().contains("unable to initialize r2dbc connectionfactory"));
    }
}
