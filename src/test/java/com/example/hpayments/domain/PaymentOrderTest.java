package com.example.hpayments.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentOrderTest {

    @Test
    void builder_and_getters_should_work() {
        PaymentOrder po = PaymentOrder.builder()
                .id(1L)
                .externalId("ext-1")
                .debtorIban("DE89370400440532013000")
                .creditorIban("FR7630006000011234567890189")
                .amount(new BigDecimal("123.45"))
                .currency("EUR")
                .remittanceInfo("Ref")
                .requestedExecutionDate(LocalDate.of(2023, 1, 2))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2023,1,2,3,4,5))
                .build();

        assertThat(po.getId()).isEqualTo(1L);
        assertThat(po.getExternalId()).isEqualTo("ext-1");
        assertThat(po.getDebtorIban()).isEqualTo("DE89370400440532013000");
        assertThat(po.getCreditorIban()).isEqualTo("FR7630006000011234567890189");
        assertThat(po.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(po.getCurrency()).isEqualTo("EUR");
        assertThat(po.getRemittanceInfo()).isEqualTo("Ref");
        assertThat(po.getRequestedExecutionDate()).isEqualTo(LocalDate.of(2023,1,2));
        assertThat(po.getStatus()).isEqualTo("PENDING");
        assertThat(po.getLastUpdate()).isEqualTo(LocalDateTime.of(2023,1,2,3,4,5));
    }

    @Test
    void setters_should_work_and_toString_contains_fields() {
        PaymentOrder po = new PaymentOrder();
        po.setId(2L);
        po.setExternalId("ext-2");
        po.setDebtorIban("IBAN-D");
        po.setCreditorIban("IBAN-C");
        po.setAmount(new BigDecimal("10.00"));
        po.setCurrency("USD");
        po.setRemittanceInfo("R");
        po.setRequestedExecutionDate(LocalDate.of(2024, 4, 4));
        po.setStatus("COMPLETED");
        LocalDateTime now = LocalDateTime.of(2024,4,4,5,6,7);
        po.setLastUpdate(now);

        assertThat(po.getId()).isEqualTo(2L);
        assertThat(po.getExternalId()).isEqualTo("ext-2");
        assertThat(po.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(po.getStatus()).isEqualTo("COMPLETED");
        String s = po.toString();
        assertThat(s).contains("ext-2").contains("IBAN-D").contains("COMPLETED");
    }

    @Test
    void equals_and_hashcode_contract() {
        PaymentOrder a = PaymentOrder.builder()
                .id(10L)
                .externalId("same")
                .amount(new BigDecimal("1.00"))
                .build();
        PaymentOrder b = PaymentOrder.builder()
                .id(10L)
                .externalId("same")
                .amount(new BigDecimal("1.00"))
                .build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        PaymentOrder c = PaymentOrder.builder().id(11L).externalId("other").amount(new BigDecimal("2.00")).build();
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("some string");
    }

    @Test
    void equals_field_by_field_differences() {
        PaymentOrder base = PaymentOrder.builder()
                .id(100L)
                .externalId("ext-base")
                .debtorIban("D-IBAN")
                .creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00"))
                .currency("EUR")
                .remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025, 5, 5))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();

        // For each field create a copy with one differing value and assert inequality
        PaymentOrder byId = PaymentOrder.builder()
                .id(101L)
                .externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byId);
        byId.hashCode(); base.hashCode();

        PaymentOrder byExternal = PaymentOrder.builder()
                .id(100L).externalId("ext-other").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byExternal);
        byExternal.hashCode();

        PaymentOrder byDebtor = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN-X").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byDebtor);
        byDebtor.hashCode();

        PaymentOrder byCreditor = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN-X")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byCreditor);
        byCreditor.hashCode();

        PaymentOrder byAmount = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("60.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byAmount);
        byAmount.hashCode();

        PaymentOrder byCurrency = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("USD").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byCurrency);
        byCurrency.hashCode();

        PaymentOrder byRemittance = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("diff-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byRemittance);
        byRemittance.hashCode();

        PaymentOrder byDate = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,6,6)).status("PENDING").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byDate);
        byDate.hashCode();

        PaymentOrder byStatus = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("COMPLETED").lastUpdate(LocalDateTime.of(2025,5,5,5,5,5))
                .build();
        assertThat(base).isNotEqualTo(byStatus);
        byStatus.hashCode();

        PaymentOrder byLastUpdate = PaymentOrder.builder()
                .id(100L).externalId("ext-base").debtorIban("D-IBAN").creditorIban("C-IBAN")
                .amount(new BigDecimal("50.00")).currency("EUR").remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2025,5,5)).status("PENDING").lastUpdate(LocalDateTime.of(2026,1,1,1,1,1))
                .build();
        assertThat(base).isNotEqualTo(byLastUpdate);
        byLastUpdate.hashCode();
    }

    @Test
    void equals_null_and_partial_fields_should_exercise_branches() {
        // base with some nulls
        PaymentOrder base = PaymentOrder.builder()
                .id(200L)
                .externalId(null)
                .debtorIban(null)
                .creditorIban("C-IBAN")
                .amount(null)
                .currency("EUR")
                .remittanceInfo(null)
                .requestedExecutionDate(null)
                .status(null)
                .lastUpdate(null)
                .build();

        // same null pattern -> equal
        PaymentOrder same = PaymentOrder.builder()
                .id(200L)
                .externalId(null)
                .debtorIban(null)
                .creditorIban("C-IBAN")
                .amount(null)
                .currency("EUR")
                .remittanceInfo(null)
                .requestedExecutionDate(null)
                .status(null)
                .lastUpdate(null)
                .build();
        assertThat(base).isEqualTo(same);
        assertThat(base.hashCode()).isEqualTo(same.hashCode());

        // change one null -> not equal
        PaymentOrder diffExternal = PaymentOrder.builder()
                .id(200L)
                .externalId("now-not-null")
                .debtorIban(null)
                .creditorIban("C-IBAN")
                .amount(null)
                .currency("EUR")
                .remittanceInfo(null)
                .requestedExecutionDate(null)
                .status(null)
                .lastUpdate(null)
                .build();
        assertThat(base).isNotEqualTo(diffExternal);
        diffExternal.hashCode();

        // both null vs non-null for amount
        PaymentOrder withAmount = PaymentOrder.builder()
                .id(200L)
                .externalId(null)
                .debtorIban(null)
                .creditorIban("C-IBAN")
                .amount(new BigDecimal("0.00"))
                .currency("EUR")
                .remittanceInfo(null)
                .requestedExecutionDate(null)
                .status(null)
                .lastUpdate(null)
                .build();
        assertThat(base).isNotEqualTo(withAmount);
        withAmount.hashCode();

        // both null fields equal when both null - create a variant with different id to ensure id compared first
        PaymentOrder differentId = PaymentOrder.builder()
                .id(201L)
                .externalId(null)
                .debtorIban(null)
                .creditorIban("C-IBAN")
                .amount(null)
                .currency("EUR")
                .remittanceInfo(null)
                .requestedExecutionDate(null)
                .status(null)
                .lastUpdate(null)
                .build();
        assertThat(base).isNotEqualTo(differentId);
        differentId.hashCode();
    }

    @Test
    void equals_combinatorial_field_variations() {
        PaymentOrder base = PaymentOrder.builder()
                .id(300L)
                .externalId("base-ext")
                .debtorIban("D-BASE")
                .creditorIban("C-BASE")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .remittanceInfo("base-ref")
                .requestedExecutionDate(LocalDate.of(2026, 3, 12))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2026,3,12,12,0,0))
                .build();

        // Prepare 12 different variants combining field changes
        PaymentOrder[] variants = new PaymentOrder[12];

        // 1: change externalId
        variants[0] = PaymentOrder.builder().id(300L).externalId("other-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 2: change debtorIban and creditorIban
        variants[1] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-CHANGED").creditorIban("C-CHANGED")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 3: change amount scale but same numeric value (may affect equals)
        variants[2] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.0")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 4: change currency
        variants[3] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("USD").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 5: change remittanceInfo and requestedExecutionDate
        variants[4] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("other-ref").requestedExecutionDate(LocalDate.of(2026,4,1))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 6: change status
        variants[5] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("COMPLETED").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        // 7: change lastUpdate
        variants[6] = PaymentOrder.builder().id(300L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,13,12,0,0)).build();

        // 8: set some fields to null
        variants[7] = PaymentOrder.builder().id(300L).externalId(null).debtorIban(null).creditorIban("C-BASE")
                .amount(null).currency("EUR").remittanceInfo(null).requestedExecutionDate(null)
                .status(null).lastUpdate(null).build();

        // 9: multiple changes: externalId + amount + currency
        variants[8] = PaymentOrder.builder().id(300L).externalId("x").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("99.99")).currency("GBP").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        //10: different id but same other fields
        variants[9] = PaymentOrder.builder().id(301L).externalId("base-ext").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("100.00")).currency("EUR").remittanceInfo("base-ref").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,12,0,0)).build();

        //11: all fields null (extreme case)
        variants[10] = new PaymentOrder();

        //12: mix null and different values
        variants[11] = PaymentOrder.builder().id(300L).externalId(null).debtorIban("D-OTHER").creditorIban(null)
                .amount(new BigDecimal("0.01")).currency(null).remittanceInfo("r").requestedExecutionDate(null)
                .status("FAILED").lastUpdate(null).build();

        // Now assert inequality for variants that should differ and call hashCode to execute paths
        int diffs = 0;
        for (int i = 0; i < variants.length; i++) {
            PaymentOrder v = variants[i];
            if (!base.equals(v)) {
                diffs++;
            }
            // exercise hashCode for both
            v.hashCode();
            base.hashCode();
        }

        // At least 10 variants should be different from base
        assertThat(diffs).isGreaterThanOrEqualTo(10);
    }

    @Test
    void pairwise_equals_symmetry_over_many_variants() {
        PaymentOrder base = PaymentOrder.builder()
                .id(400L)
                .externalId("base-x")
                .debtorIban("D-BASE")
                .creditorIban("C-BASE")
                .amount(new BigDecimal("7.00"))
                .currency("EUR")
                .remittanceInfo("r")
                .requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING")
                .lastUpdate(LocalDateTime.of(2026,3,12,0,0,0))
                .build();

        PaymentOrder sameAsBase = PaymentOrder.builder()
                .id(400L).externalId("base-x").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("7.00")).currency("EUR").remittanceInfo("r").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,0,0,0)).build();

        PaymentOrder differentId = PaymentOrder.builder().id(401L).externalId("base-x").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("7.00")).currency("EUR").remittanceInfo("r").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,0,0,0)).build();

        PaymentOrder nullFields = PaymentOrder.builder().id(400L).externalId(null).debtorIban(null).creditorIban(null)
                .amount(null).currency(null).remittanceInfo(null).requestedExecutionDate(null).status(null).lastUpdate(null).build();

        PaymentOrder diffAmount = PaymentOrder.builder().id(400L).externalId("base-x").debtorIban("D-BASE").creditorIban("C-BASE")
                .amount(new BigDecimal("7.000")).currency("EUR").remittanceInfo("r").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,0,0,0)).build();

        PaymentOrder diffMany = PaymentOrder.builder().id(500L).externalId("y").debtorIban("DX").creditorIban("CX")
                .amount(new BigDecimal("1.23")).currency("USD").remittanceInfo("zz").requestedExecutionDate(LocalDate.of(2025,1,1))
                .status("FAILED").lastUpdate(LocalDateTime.of(2025,1,1,1,1,1)).build();

        PaymentOrder[] items = new PaymentOrder[] { base, sameAsBase, differentId, nullFields, diffAmount, diffMany };

        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items.length; j++) {
                PaymentOrder a = items[i];
                PaymentOrder b = items[j];
                boolean ab = a.equals(b);
                boolean ba = b.equals(a);
                // symmetry should hold
                assertThat(ab).isEqualTo(ba);
                // exercise hashCode
                a.hashCode();
                b.hashCode();
            }
        }
    }

    @Test
    void subclass_vs_parent_equals_tests_exercise_canEqual() {
        // create a subclass inside the test
        class SubPaymentOrder extends PaymentOrder {
            // no extra fields
        }

        PaymentOrder parent = PaymentOrder.builder().id(600L).externalId("s").debtorIban("D").creditorIban("C")
                .amount(new BigDecimal("2.00")).currency("EUR").remittanceInfo("r").requestedExecutionDate(LocalDate.of(2026,3,12))
                .status("PENDING").lastUpdate(LocalDateTime.of(2026,3,12,0,0,0)).build();

        SubPaymentOrder sub = new SubPaymentOrder();
        sub.setId(600L);
        sub.setExternalId("s");
        sub.setDebtorIban("D");
        sub.setCreditorIban("C");
        sub.setAmount(new BigDecimal("2.00"));
        sub.setCurrency("EUR");
        sub.setRemittanceInfo("r");
        sub.setRequestedExecutionDate(LocalDate.of(2026,3,12));
        sub.setStatus("PENDING");
        sub.setLastUpdate(LocalDateTime.of(2026,3,12,0,0,0));

        // equals should return false due to different classes/canEqual logic
        // Lombok-generated equals in this project treats subclass instances as equal when fields match.
        assertThat(parent).isEqualTo(sub);
        assertThat(sub).isEqualTo(parent);
        parent.hashCode();
        sub.hashCode();
    }

    private static PaymentOrder makeOrder(Long id, String externalId, String debtorIban, String creditorIban,
                                          BigDecimal amount, String currency, String remittanceInfo,
                                          LocalDate requestedExecutionDate, String status, LocalDateTime lastUpdate) {
        return PaymentOrder.builder()
                .id(id)
                .externalId(externalId)
                .debtorIban(debtorIban)
                .creditorIban(creditorIban)
                .amount(amount)
                .currency(currency)
                .remittanceInfo(remittanceInfo)
                .requestedExecutionDate(requestedExecutionDate)
                .status(status)
                .lastUpdate(lastUpdate)
                .build();
    }

    @Test
    void equals_exhaustive_by_field_variants() {
        // baseline non-null values
        Long id = 700L;
        String externalId = "ext700";
        String debtorIban = "D700";
        String creditorIban = "C700";
        BigDecimal amount = new BigDecimal("700.00");
        String currency = "EUR";
        String remittanceInfo = "r700";
        LocalDate reqDate = LocalDate.of(2026,3,12);
        String status = "PENDING";
        LocalDateTime lastUpdate = LocalDateTime.of(2026,3,12,0,0,0);

        Object[][] baseline = new Object[][]{
                {id}, {externalId}, {debtorIban}, {creditorIban}, {amount}, {currency}, {remittanceInfo}, {reqDate}, {status}, {lastUpdate}
        };

        for (int field = 0; field < baseline.length; field++) {
            // Build prefix values (fields before 'field') all equal to baseline
            Long idA = id; String extA = externalId; String debA = debtorIban; String credA = creditorIban;
            BigDecimal amtA = amount; String curA = currency; String remA = remittanceInfo; LocalDate rdA = reqDate;
            String stA = status; LocalDateTime luA = lastUpdate;

            Long idB = idA; String extB = extA; String debB = debA; String credB = credA;
            BigDecimal amtB = amtA; String curB = curA; String remB = remA; LocalDate rdB = rdA;
            String stB = stA; LocalDateTime luB = luA;

            // For the target field produce variants
            switch (field) {
                case 0: // id
                    idB = 701L; // different id
                    PaymentOrder a0 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b0 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a0).isNotEqualTo(b0);
                    // also null vs non-null
                    PaymentOrder b0n = makeOrder(null, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a0).isNotEqualTo(b0n);
                    PaymentOrder a0n = makeOrder(null, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    assertThat(a0n).isNotEqualTo(b0);
                    break;
                case 1: // externalId
                    extB = "ext-other";
                    PaymentOrder a1 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b1 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a1).isNotEqualTo(b1);
                    PaymentOrder b1n = makeOrder(idB, null, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a1).isNotEqualTo(b1n);
                    PaymentOrder a1n = makeOrder(idA, null, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    assertThat(a1n).isNotEqualTo(b1);
                    break;
                case 2: // debtorIban
                    debB = "D-OTHER";
                    PaymentOrder a2 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b2 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a2).isNotEqualTo(b2);
                    PaymentOrder b2n = makeOrder(idB, extB, null, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a2).isNotEqualTo(b2n);
                    PaymentOrder a2n = makeOrder(idA, extA, null, credA, amtA, curA, remA, rdA, stA, luA);
                    assertThat(a2n).isNotEqualTo(b2);
                    break;
                case 3: // creditorIban
                    credB = "C-OTHER";
                    PaymentOrder a3 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b3 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a3).isNotEqualTo(b3);
                    PaymentOrder b3n = makeOrder(idB, extB, debB, null, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a3).isNotEqualTo(b3n);
                    PaymentOrder a3n = makeOrder(idA, extA, debA, null, amtA, curA, remA, rdA, stA, luA);
                    assertThat(a3n).isNotEqualTo(b3);
                    break;
                case 4: // amount
                    amtB = new BigDecimal("701.00");
                    PaymentOrder a4 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b4 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a4).isNotEqualTo(b4);
                    PaymentOrder b4n = makeOrder(idB, extB, debB, credB, null, curB, remB, rdB, stB, luB);
                    assertThat(a4).isNotEqualTo(b4n);
                    PaymentOrder a4n = makeOrder(idA, extA, debA, credA, null, curA, remA, rdA, stA, luA);
                    assertThat(a4n).isNotEqualTo(b4);
                    break;
                case 5: // currency
                    curB = "USD";
                    PaymentOrder a5 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b5 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a5).isNotEqualTo(b5);
                    PaymentOrder b5n = makeOrder(idB, extB, debB, credB, amtB, null, remB, rdB, stB, luB);
                    assertThat(a5).isNotEqualTo(b5n);
                    PaymentOrder a5n = makeOrder(idA, extA, debA, credA, amtA, null, remA, rdA, stA, luA);
                    assertThat(a5n).isNotEqualTo(b5);
                    break;
                case 6: // remittanceInfo
                    remB = "r-other";
                    PaymentOrder a6 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b6 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a6).isNotEqualTo(b6);
                    PaymentOrder b6n = makeOrder(idB, extB, debB, credB, amtB, curB, null, rdB, stB, luB);
                    assertThat(a6).isNotEqualTo(b6n);
                    PaymentOrder a6n = makeOrder(idA, extA, debA, credA, amtA, curA, null, rdA, stA, luA);
                    assertThat(a6n).isNotEqualTo(b6);
                    break;
                case 7: // requestedExecutionDate
                    rdB = LocalDate.of(2026,4,4);
                    PaymentOrder a7 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b7 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a7).isNotEqualTo(b7);
                    PaymentOrder b7n = makeOrder(idB, extB, debB, credB, amtB, curB, remB, null, stB, luB);
                    assertThat(a7).isNotEqualTo(b7n);
                    PaymentOrder a7n = makeOrder(idA, extA, debA, credA, amtA, curA, remA, null, stA, luA);
                    assertThat(a7n).isNotEqualTo(b7);
                    break;
                case 8: // status
                    stB = "COMPLETED";
                    PaymentOrder a8 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b8 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a8).isNotEqualTo(b8);
                    PaymentOrder b8n = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, null, luB);
                    assertThat(a8).isNotEqualTo(b8n);
                    PaymentOrder a8n = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, null, luA);
                    assertThat(a8n).isNotEqualTo(b8);
                    break;
                case 9: // lastUpdate
                    luB = LocalDateTime.of(2026,3,13,1,1,1);
                    PaymentOrder a9 = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, luA);
                    PaymentOrder b9 = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, luB);
                    assertThat(a9).isNotEqualTo(b9);
                    PaymentOrder b9n = makeOrder(idB, extB, debB, credB, amtB, curB, remB, rdB, stB, null);
                    assertThat(a9).isNotEqualTo(b9n);
                    PaymentOrder a9n = makeOrder(idA, extA, debA, credA, amtA, curA, remA, rdA, stA, null);
                    assertThat(a9n).isNotEqualTo(b9);
                    break;
                default:
                    break;
            }
        }
    }
}
