package com.example.hpayments.adapter.inbound;

import com.example.hpayments.adapter.outbound.SpringDataAuditLogRepository;
import com.example.hpayments.domain.AuditLogEntry;
import com.example.hpayments.util.AuditMaskingUtil;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class AuditWebFilter implements WebFilter {

    private final SpringDataAuditLogRepository repository;
    private final ObservationRegistry observationRegistry;

    // Use ObjectProvider to allow the bean to be absent in test slices; call getIfAvailable()
    public AuditWebFilter(ObjectProvider<SpringDataAuditLogRepository> repositoryProvider,
                          ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        this.repository = repositoryProvider.getIfAvailable();
        this.observationRegistry = observationRegistryProvider.getIfAvailable();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String headerVal = request.getHeaders().getFirst("X-Request-ID");
        final String requestId = (headerVal == null || headerVal.isEmpty()) ? UUID.randomUUID().toString() : headerVal;

        // echo requestId back to client
        response.getHeaders().add("X-Request-ID", requestId);

        // Extract trace id from common tracing headers (W3C traceparent, B3, or Cloud Trace)
        String traceId = extractTraceIdFromHeaders(request);
        if (traceId != null) {
            MDC.put("traceId", traceId);
        }

        // Put in MDC for synchronous logging bridges
        MDC.put("requestId", requestId);

        Instant start = Instant.now();

        // Prepare Micrometer Observation if available
        final Observation obs = observationRegistry != null ? Observation.createNotStarted("http.server.requests", observationRegistry) : null;
        if (obs != null) {
            obs.lowCardinalityKeyValue("request.id", requestId);
            if (traceId != null) obs.lowCardinalityKeyValue("trace.id", traceId);
            obs.lowCardinalityKeyValue("path", request.getPath().pathWithinApplication().value());
            obs.lowCardinalityKeyValue("method", request.getMethod() != null ? request.getMethod().name() : "");
            obs.start();
        }

        // Obtain username from ServerWebExchange principal if available
        Mono<String> usernameMono = exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous");

        return usernameMono.flatMap(username ->
                // propagate requestId/traceId in Reactor Context so downstream instrumentation (Micrometer/OTel) can pick it up
                chain.filter(exchange)
                        .contextWrite(ctx -> {
                            ctx = ctx.put("requestId", requestId);
                            if (traceId != null) ctx = ctx.put("traceId", traceId);
                            return ctx;
                        })
                        .doOnError(throwable -> {
                            if (obs != null) obs.lowCardinalityKeyValue("error", throwable.getClass().getSimpleName());
                            saveAudit(exchange, requestId, start, username, throwable);
                        })
                        .doOnSuccess(aVoid -> saveAudit(exchange, requestId, start, username, null))
                        .doFinally(sig -> {
                            if (obs != null) {
                                // add status tag if available
                                Integer status = response.getStatusCode() != null ? response.getStatusCode().value() : null;
                                if (status != null) obs.lowCardinalityKeyValue("status", String.valueOf(status));
                                obs.stop();
                            }
                        })
        ).doFinally(sig -> {
            MDC.remove("requestId");
            if (traceId != null) MDC.remove("traceId");
        });
    }

    private void saveAudit(ServerWebExchange exchange, String requestId, Instant start, String username, Throwable throwable) {
        // If repository is not available (e.g., in slice tests), skip persistence
        if (this.repository == null) {
            return;
        }

        Instant end = Instant.now();
        long elapsed = Duration.between(start, end).toMillis();

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        Integer status = response.getStatusCode() != null ? response.getStatusCode().value() : null;
        String path = request.getPath().pathWithinApplication().value();
        String method = request.getMethod() != null ? request.getMethod().name() : null;

        String errorMessage = throwable != null ? throwable.getMessage() : null;

        String payload = ""; // For now we won't capture full body (requires caching read)
        payload = AuditMaskingUtil.maskSensitive(payload);

        String traceId = MDC.get("traceId");

        AuditLogEntry entry = AuditLogEntry.builder()
                .requestId(requestId)
                .path(path)
                .method(method)
                .username(username)
                .status(status)
                .errorMessage(errorMessage)
                .payload(payload)
                .startedAt(start)
                .endedAt(end)
                .elapsedMs(elapsed)
                .traceId(traceId)
                .build();

        // Persist asynchronously
        try {
            repository.save(entry).subscribe();
        } catch (Exception ex) {
            // Don't let persistence issues break request handling
            // log via stdout/stderr if logging available
            System.err.println("Failed to save audit entry: " + ex.getMessage());
        }
    }

    private String extractTraceIdFromHeaders(ServerHttpRequest request) {
        // W3C Trace Context header: traceparent: "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
        String traceparent = request.getHeaders().getFirst("traceparent");
        if (traceparent != null && !traceparent.isEmpty()) {
            try {
                String[] parts = traceparent.split("-");
                if (parts.length >= 3) {
                    return parts[1];
                }
            } catch (Exception ignored) {}
        }

        // B3 single header or separate headers
        String b3 = request.getHeaders().getFirst("b3");
        if (b3 != null && !b3.isEmpty()) {
            String[] parts = b3.split("-");
            if (parts.length >= 1) return parts[0];
        }

        String xB3 = request.getHeaders().getFirst("X-B3-TraceId");
        if (xB3 != null && !xB3.isEmpty()) return xB3;

        // Google Cloud Trace header: X-Cloud-Trace-Context: TRACE_ID/SPAN_ID;o=TRACE_TRUE
        String cloud = request.getHeaders().getFirst("X-Cloud-Trace-Context");
        if (cloud != null && !cloud.isEmpty()) {
            int slash = cloud.indexOf('/');
            if (slash > 0) return cloud.substring(0, slash);
        }

        return null;
    }
}
