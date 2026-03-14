package com.example.hpayments.util;

import java.util.regex.Pattern;

public final class AuditMaskingUtil {

    private static final Pattern CARD_PATTERN = Pattern.compile("(\\d{4})(\\d{8,12})(\\d{4})");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\b\\d{3,4}\\b");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(token|access_token|authorization)\\\"?\\s*[:=]\\s*\\\"?([\\w-\\.]+)\\\"?");

    private AuditMaskingUtil() {}

    public static String maskSensitive(String input) {
        if (input == null || input.isEmpty()) return input;

        String out = input;

        // mask card numbers keeping first 4 and last 4
        out = CARD_PATTERN.matcher(out).replaceAll("$1********$3");

        // mask tokens/authorization values
        out = TOKEN_PATTERN.matcher(out).replaceAll("$1=****");

        // CVV naive masking: replace isolated 3-4 digit groups with ***
        out = CVV_PATTERN.matcher(out).replaceAll("***");

        return out;
    }
}
