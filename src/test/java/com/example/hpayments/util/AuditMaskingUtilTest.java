package com.example.hpayments.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuditMaskingUtilTest {

    @Test
    void mask_null_returnsNull() {
        assertNull(AuditMaskingUtil.maskSensitive(null));
    }

    @Test
    void mask_empty_returnsEmpty() {
        assertEquals("", AuditMaskingUtil.maskSensitive(""));
    }

    @Test
    void mask_nonSensitive_returnsSame() {
        String input = "This is a harmless message with letters and punctuation.";
        assertEquals(input, AuditMaskingUtil.maskSensitive(input));
    }

    @Test
    void mask_card16_masksMiddle() {
        String card = "4111111111111111"; // 16 digits
        String masked = AuditMaskingUtil.maskSensitive(card);
        // Implementation currently masks into asterisks; assert that original is gone and masked contains only asterisks
        assertFalse(masked.contains("4111111111111111"));
        assertTrue(masked.matches("^\\*+$"));
    }

    @Test
    void mask_card20_masksMiddle() {
        String card = "12345678123456781234"; // 20 digits
        String masked = AuditMaskingUtil.maskSensitive(card);
        assertFalse(masked.contains(card));
        assertTrue(masked.matches("^\\*+$"));
    }

    @Test
    void mask_card_with_spaces_transforms_groups() {
        String spaced = "4111 1111 1111 1111";
        String masked = AuditMaskingUtil.maskSensitive(spaced);
        // Implementation replaces grouped digits with '***' groups
        assertEquals("*** *** *** ***", masked);
    }

    @Test
    void mask_card_with_dashes_transforms_groups() {
        String dashed = "4111-1111-1111-1111";
        String masked = AuditMaskingUtil.maskSensitive(dashed);
        assertEquals("***-***-***-***", masked);
    }

    @Test
    void mask_short_number_not_masked() {
        String fifteen = "123456789012345"; // 15 digits, below pattern minimum
        assertEquals(fifteen, AuditMaskingUtil.maskSensitive(fifteen));
    }

    @Test
    void mask_long_number_partial_mask() {
        String twentyOne = "123456781234567812345"; // 21 digits
        String masked = AuditMaskingUtil.maskSensitive(twentyOne);
        assertFalse(masked.contains(twentyOne));
        // expect some digits remain at the end
        assertTrue(masked.endsWith("12345"));
    }

    @Test
    void mask_cvv_groups_replaced() {
        String withCvv = "cvv=123 and alt=9876";
        String masked = AuditMaskingUtil.maskSensitive(withCvv);
        assertTrue(masked.contains("cvv=***"));
        assertTrue(masked.contains("alt=***"));
    }

    @Test
    void mask_cvv_does_not_mask_longer_groups() {
        String id = "id=12345"; // 5-digit group should not be masked
        assertEquals(id, AuditMaskingUtil.maskSensitive(id));

        String pin = "pin=0123"; // 4-digit isolated should be masked
        assertTrue(AuditMaskingUtil.maskSensitive(pin).contains("pin=***"));
    }

    @Test
    void mask_token_variants_masked() {
        String t1 = "token:\"abc123-DEF.456\"";
        String t2 = "access_token=tok.value-123";
        String t3 = "authorization:\"Secr3t\"";

        assertTrue(AuditMaskingUtil.maskSensitive(t1).startsWith("token="));
        assertTrue(AuditMaskingUtil.maskSensitive(t1).contains("****"));
        assertTrue(AuditMaskingUtil.maskSensitive(t2).contains("access_token=****"));
        assertTrue(AuditMaskingUtil.maskSensitive(t3).contains("authorization=****"));
    }

    @Test
    void mask_token_in_json_surrounding_punctuation() {
        String json = "{\"token\": \"abc123.DEF-456\", \"other\": 1}";
        String masked = AuditMaskingUtil.maskSensitive(json);
        assertFalse(masked.contains("abc123.DEF-456"));
        assertTrue(masked.contains("token=") || masked.contains("token"));
    }

    @Test
    void mask_token_case_insensitive_preserves_case_in_key() {
        String input = "ToKeN=AbC123";
        String masked = AuditMaskingUtil.maskSensitive(input);
        assertTrue(masked.toLowerCase().contains("token="));
    }

    @Test
    void mask_token_with_unallowed_chars_partial_mask() {
        String t = "access_token=ab+c/123"; // plus and slash
        String masked = AuditMaskingUtil.maskSensitive(t);
        // Partial masking expected: token value portion up to '+' replaced
        assertTrue(masked.startsWith("access_token=****"));
        assertTrue(masked.contains("+c/"));
    }

    @Test
    void mask_mixed_payload_masks_all() {
        String payload = "{" +
                "\"cardNumber\": \"4111111111111111\", " +
                "\"cvv\": \"123\", " +
                "\"access_token\": \"abc.def-123\"}";

        String masked = AuditMaskingUtil.maskSensitive(payload);

        assertFalse(masked.contains("4111111111111111"));
        assertFalse(masked.contains("\"123\""));
        assertFalse(masked.contains("abc.def-123"));

        assertTrue(masked.contains("***") || masked.matches(".*\\*{6,}.*"));
    }

    // --- Additional edge-case tests to increase coverage ---

    @Test
    void mask_multiple_tokens_all_masked() {
        String in = "token:aaa access_token=bbb authorization=ccc";
        String out = AuditMaskingUtil.maskSensitive(in);
        assertFalse(out.contains("aaa"));
        assertFalse(out.contains("bbb"));
        assertFalse(out.contains("ccc"));
        // at least three masked markers
        int count = out.split("\\*\\*\\*\\*").length - 1;
        assertTrue(count >= 3 || out.contains("****"));
    }

    @Test
    void mask_card_mixed_separators() {
        String in = "4111-1111 1111/1111";
        String out = AuditMaskingUtil.maskSensitive(in);
        // expect no raw 4-digit groups remain
        assertFalse(out.matches(".*\\d{4}.*"));
        assertTrue(out.contains("***"));
    }

    @Test
    void mask_no_false_positive_inside_words() {
        String in = "abc1234def";
        // 4-digit sequence inside letters should not be considered isolated CVV
        assertEquals(in, AuditMaskingUtil.maskSensitive(in));
    }

    @Test
    void mask_text_with_two_cards() {
        String in = "4111111111111111, 5500000000000004";
        String out = AuditMaskingUtil.maskSensitive(in);
        assertFalse(out.contains("4111111111111111"));
        assertFalse(out.contains("5500000000000004"));
        assertTrue(out.contains("****") || out.matches(".*\\*{6,}.*"));
    }

}