package com.example.hpayments.adapters.persistence;

import com.example.hpayments.domain.PaymentOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentOrderRepositoryAdapterTest {

    private SpringDataPaymentOrderRepository repository;
    private PaymentOrderRepositoryAdapter adapter;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(SpringDataPaymentOrderRepository.class);
        adapter = new PaymentOrderRepositoryAdapter(repository);
    }

    @Test
    void save_mapsAndReturnsPaymentOrderWithIdAndLastUpdate() {
        PaymentOrder input = PaymentOrder.builder()
                .externalId("ext-1")
                .debtorIban("DE123")
                .creditorIban("FR456")
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .remittanceInfo("memo")
                .requestedExecutionDate(LocalDate.of(2026, 3, 13))
                .status("PENDING")
                .build();

        SpringDataPaymentOrderEntity saved = SpringDataPaymentOrderEntity.builder()
                .id(42L)
                .externalId("ext-1")
                .lastUpdate(LocalDateTime.of(2026,3,13,12,0))
                .build();

        when(repository.save(any(SpringDataPaymentOrderEntity.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(adapter.save(input))
                .expectNextMatches(po -> po.getId() != null && po.getId().equals(42L) && po.getLastUpdate().equals(saved.getLastUpdate()))
                .verifyComplete();
    }

    @Test
    void findById_returnsMappedPaymentOrder() {
        SpringDataPaymentOrderEntity entity = SpringDataPaymentOrderEntity.builder()
                .id(55L)
                .externalId("ext-55")
                .debtorIban("DE1")
                .creditorIban("CR1")
                .amount(new BigDecimal("5.00"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026,3,14))
                .status("DONE")
                .lastUpdate(LocalDateTime.of(2026,3,14,10,0))
                .build();

        when(repository.findByExternalId("ext-55")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById("ext-55"))
                .expectNextMatches(po -> po.getId().equals(55L) && po.getExternalId().equals("ext-55") && po.getStatus().equals("DONE"))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(repository.findByExternalId("nope")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("nope"))
                .verifyComplete();
    }

    @Test
    void save_propagatesErrorFromRepository() {
        PaymentOrder input = PaymentOrder.builder().externalId("e").build();
        when(repository.save(any(SpringDataPaymentOrderEntity.class))).thenReturn(Mono.error(new RuntimeException("db")));

        StepVerifier.create(adapter.save(input))
                .expectErrorMessage("db")
                .verify();
    }

    // Edge case tests: partially null fields
    @Test
    void save_withNullAmount_handlesNullAmount() {
        PaymentOrder input = PaymentOrder.builder()
                .externalId("ext-null-amount")
                .debtorIban("DE123")
                .creditorIban("FR456")
                .amount(null)
                .currency("EUR")
                .remittanceInfo("memo")
                .requestedExecutionDate(LocalDate.of(2026, 3, 13))
                .status("PENDING")
                .build();

        SpringDataPaymentOrderEntity saved = SpringDataPaymentOrderEntity.builder()
                .id(100L)
                .externalId("ext-null-amount")
                .lastUpdate(LocalDateTime.of(2026,3,13,12,0))
                .amount(null)
                .build();

        when(repository.save(any(SpringDataPaymentOrderEntity.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(adapter.save(input))
                .expectNextMatches(po -> po.getId().equals(100L) && po.getAmount() == null)
                .verifyComplete();
    }

    @Test
    void save_withNullRequestedExecutionDate_handlesNullDate() {
        PaymentOrder input = PaymentOrder.builder()
                .externalId("ext-null-date")
                .debtorIban("DE123")
                .creditorIban("FR456")
                .amount(new BigDecimal("1.00"))
                .currency("EUR")
                .remittanceInfo("memo")
                .requestedExecutionDate(null)
                .status("PENDING")
                .build();

        SpringDataPaymentOrderEntity saved = SpringDataPaymentOrderEntity.builder()
                .id(101L)
                .externalId("ext-null-date")
                .lastUpdate(LocalDateTime.of(2026,3,13,12,0))
                .requestedExecutionDate(null)
                .build();

        when(repository.save(any(SpringDataPaymentOrderEntity.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(adapter.save(input))
                .expectNextMatches(po -> po.getId().equals(101L) && po.getRequestedExecutionDate() == null)
                .verifyComplete();
    }

    @Test
    void findById_withNullAmountAndDate_mapsNulls() {
        SpringDataPaymentOrderEntity entity = SpringDataPaymentOrderEntity.builder()
                .id(202L)
                .externalId("ext-202")
                .amount(null)
                .requestedExecutionDate(null)
                .status("NEW")
                .lastUpdate(LocalDateTime.of(2026,3,15,9,0))
                .build();

        when(repository.findByExternalId("ext-202")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById("ext-202"))
                .expectNextMatches(po -> po.getId().equals(202L) && po.getAmount() == null && po.getRequestedExecutionDate() == null)
                .verifyComplete();
    }
}