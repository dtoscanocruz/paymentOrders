package com.example.hpayments;

import com.example.hpayments.adapters.persistence.PaymentOrderRepositoryAdapter;
import com.example.hpayments.adapters.persistence.SpringDataPaymentOrderEntity;
import com.example.hpayments.adapters.persistence.SpringDataPaymentOrderRepository;
import com.example.hpayments.domain.PaymentOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@DataR2dbcTest
@Import({com.example.hpayments.config.R2dbcConfiguration.class, PaymentOrderRepositoryAdapter.class})
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
public class PaymentOrderRepositoryIntegrationTest {

    @Autowired
    private SpringDataPaymentOrderRepository repository;

    @Autowired
    private PaymentOrderRepositoryAdapter adapter;

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void repositorySaveAndFindByExternalId() {
        // create table
        databaseClient.sql("CREATE TABLE IF NOT EXISTS payment_orders (id BIGINT PRIMARY KEY AUTO_INCREMENT, external_id VARCHAR(255) UNIQUE, debtor_iban VARCHAR(255), creditor_iban VARCHAR(255), amount DECIMAL(19,2), currency VARCHAR(10), remittance_info VARCHAR(1024), requested_execution_date DATE, status VARCHAR(50), last_update TIMESTAMP)")
                .fetch()
                .rowsUpdated()
                .block();

        String externalId = "ext-123";
        SpringDataPaymentOrderEntity entity = SpringDataPaymentOrderEntity.builder()
                .externalId(externalId)
                .debtorIban("DE0012345678")
                .creditorIban("DE0098765432")
                .amount(new BigDecimal("123.45"))
                .currency("EUR")
                .remittanceInfo("invoice 1")
                .requestedExecutionDate(LocalDate.now())
                .status("PENDING")
                .lastUpdate(LocalDateTime.now())
                .build();

        StepVerifier.create(repository.save(entity))
                .assertNext(saved -> {
                    assert saved.getId() != null;
                    assert saved.getExternalId().equals(externalId);
                })
                .verifyComplete();

        StepVerifier.create(repository.findByExternalId(externalId))
                .assertNext(found -> {
                    assert found.getExternalId().equals(externalId);
                    assert found.getAmount().compareTo(new BigDecimal("123.45")) == 0;
                })
                .verifyComplete();
    }

    @Test
    void adapterSaveAndFindById() {
        // ensure table exists
        databaseClient.sql("CREATE TABLE IF NOT EXISTS payment_orders (id BIGINT PRIMARY KEY AUTO_INCREMENT, external_id VARCHAR(255) UNIQUE, debtor_iban VARCHAR(255), creditor_iban VARCHAR(255), amount DECIMAL(19,2), currency VARCHAR(10), remittance_info VARCHAR(1024), requested_execution_date DATE, status VARCHAR(50), last_update TIMESTAMP)")
                .fetch()
                .rowsUpdated()
                .block();

        String externalId = "adapter-ext-1";
        PaymentOrder domain = PaymentOrder.builder()
                .externalId(externalId)
                .debtorIban("DE000111222")
                .creditorIban("DE000333444")
                .amount(new BigDecimal("50.00"))
                .currency("EUR")
                .remittanceInfo("payment")
                .requestedExecutionDate(LocalDate.now())
                .status("NEW")
                .build();

        StepVerifier.create(adapter.save(domain))
                .assertNext(saved -> {
                    // adapter populates id and lastUpdate
                    assert saved.getId() != null;
                    assert saved.getExternalId().equals(externalId);
                    assert saved.getLastUpdate() != null;
                })
                .verifyComplete();

        StepVerifier.create(adapter.findById(externalId))
                .assertNext(found -> {
                    assert found.getExternalId().equals(externalId);
                    assert found.getAmount().compareTo(new BigDecimal("50.00")) == 0;
                })
                .verifyComplete();
    }
}
