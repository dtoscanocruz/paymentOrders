package com.example.hpayments.adapters.rest.exception;

import com.example.hpayments.rest.adapter.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.validation.FieldError;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setCode("VALIDATION_ERROR");
        // build a concise message with field errors
        List<String> messages = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        err.setMessage("Validation failed: " + String.join("; ", messages));
        // also include a details map with field->message
        Map<String, String> fieldMap = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + ", " + b));
        err.setDetails((Map) fieldMap);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setCode("BAD_REQUEST");
        err.setMessage(ex.getMessage() == null ? "Bad request" : ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        // Log the full stacktrace to help tests surface the root cause
        log.error("Unhandled exception in request processing", ex);

        ErrorResponse err = new ErrorResponse();
        err.setCode("INTERNAL_ERROR");
        String msg = "Internal server error";
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            msg = msg + ": " + ex.getMessage();
        }
        err.setMessage(msg);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err));
    }
}