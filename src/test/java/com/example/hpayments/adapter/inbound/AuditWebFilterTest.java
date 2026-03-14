package com.example.hpayments.adapter.inbound;

import com.example.hpayments.adapter.outbound.SpringDataAuditLogRepository;
import com.example.hpayments.domain.AuditLogEntry;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AuditWebFilterTest {

    @Test
    void extractTraceIdFromTraceparentHeader() throws Exception {
        MockServerHttpRequest req = MockServerHttpRequest.get("/api/foo")
                .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        ObjectProvider<SpringDataAuditLogRepository> repoProvider = mock(ObjectProvider.class);
        ObjectProvider<ObservationRegistry> obsProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(null);
        when(obsProvider.getIfAvailable()).thenReturn(null);

        AuditWebFilter filter = new AuditWebFilter(repoProvider, obsProvider);
        Method m = AuditWebFilter.class.getDeclaredMethod("extractTraceIdFromHeaders", ServerHttpRequest.class);
        m.setAccessible(true);
        String traceId = (String) m.invoke(filter, exchange.getRequest());
        assertThat(traceId).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
    }

    @Test
    void extractTraceIdFromB3Header() throws Exception {
        MockServerHttpRequest req = MockServerHttpRequest.get("/x")
                .header("b3", "abcd-1-1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        ObjectProvider<SpringDataAuditLogRepository> repoProvider = mock(ObjectProvider.class);
        ObjectProvider<ObservationRegistry> obsProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(null);
        when(obsProvider.getIfAvailable()).thenReturn(null);

        AuditWebFilter filter = new AuditWebFilter(repoProvider, obsProvider);
        Method m = AuditWebFilter.class.getDeclaredMethod("extractTraceIdFromHeaders", ServerHttpRequest.class);
        m.setAccessible(true);
        String traceId = (String) m.invoke(filter, exchange.getRequest());
        assertThat(traceId).isEqualTo("abcd");
    }

    @Test
    void filter_echosRequestId_and_skipsPersistence_whenNoRepository() {
        MockServerHttpRequest req = MockServerHttpRequest.get("/hello").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        ObjectProvider<SpringDataAuditLogRepository> repoProvider = mock(ObjectProvider.class);
        ObjectProvider<ObservationRegistry> obsProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(null);
        when(obsProvider.getIfAvailable()).thenReturn(null);

        AuditWebFilter filter = new AuditWebFilter(repoProvider, obsProvider);

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        // Response has X-Request-ID header
        String header = exchange.getResponse().getHeaders().getFirst("X-Request-ID");
        assertThat(header).isNotNull();
    }

    @Test
    void saveAudit_callsRepositorySave_whenRepositoryPresent() {
        SpringDataAuditLogRepository repo = mock(SpringDataAuditLogRepository.class);
        when(repo.save(any(AuditLogEntry.class))).thenReturn(Mono.empty());

        ObjectProvider<SpringDataAuditLogRepository> repoProvider = mock(ObjectProvider.class);
        ObjectProvider<ObservationRegistry> obsProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(repo);
        when(obsProvider.getIfAvailable()).thenReturn(null);

        // provider that returns repo
        AuditWebFilter filter = new AuditWebFilter(repoProvider, obsProvider);

        MockServerHttpRequest req = MockServerHttpRequest.get("/pay").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);
        exchange.getResponse().setStatusCode(HttpStatus.OK);

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        // allow async save to subscribe; since repository.save subscribes in filter, verify invocation
        verify(repo, timeout(1000).times(1)).save(any(AuditLogEntry.class));
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repo).save(captor.capture());

        AuditLogEntry entry = captor.getValue();
        assertThat(entry.getPath()).isEqualTo("/pay");
        assertThat(entry.getRequestId()).isNotNull();
        assertThat(entry.getStartedAt()).isNotNull();
    }
}