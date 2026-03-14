package com.example.hpayments.contract.adapters;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateAdaptersTest {

    @Test
    void localDate_unmarshal_withValidString_returnsLocalDate() throws Exception {
        LocalDateAdapter adapter = new LocalDateAdapter();
        LocalDate d = adapter.unmarshal("2026-03-12");
        assertThat(d).isEqualTo(LocalDate.of(2026, 3, 12));
    }

    @Test
    void localDate_marshal_withValidDate_returnsString() throws Exception {
        LocalDateAdapter adapter = new LocalDateAdapter();
        String s = adapter.marshal(LocalDate.of(2026, 3, 12));
        assertThat(s).isEqualTo("2026-03-12");
    }

    @Test
    void localDate_null_handling() throws Exception {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThat(adapter.unmarshal(null)).isNull();
        assertThat(adapter.marshal(null)).isNull();
    }

    @Test
    void localDate_unmarshal_invalid_throws() {
        LocalDateAdapter adapter = new LocalDateAdapter();
        assertThrows(Exception.class, () -> adapter.unmarshal("not-a-date"));
    }

    @Test
    void localDateTime_unmarshal_withValidString_returnsLocalDateTime() throws Exception {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        LocalDateTime dt = adapter.unmarshal("2026-03-12T10:15:30");
        assertThat(dt).isEqualTo(LocalDateTime.of(2026, 3, 12, 10, 15, 30));
    }

    @Test
    void localDateTime_marshal_withValidDateTime_returnsString() throws Exception {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        String s = adapter.marshal(LocalDateTime.of(2026, 3, 12, 10, 15, 30));
        assertThat(s).isEqualTo("2026-03-12T10:15:30");
    }

    @Test
    void localDateTime_null_handling() throws Exception {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThat(adapter.unmarshal(null)).isNull();
        assertThat(adapter.marshal(null)).isNull();
    }

    @Test
    void localDateTime_unmarshal_invalid_throws() {
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        assertThrows(Exception.class, () -> adapter.unmarshal("not-a-datetime"));
    }
}
