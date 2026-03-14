package com.example.hpayments.adapters.rest.exception;

import com.example.hpayments.rest.adapter.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_buildsBadRequestWithDetails() {
        FieldError fe1 = new FieldError("target", "name", "must not be empty");
        FieldError fe2 = new FieldError("target", "amount", "must be positive");
        List<FieldError> fieldErrors = Arrays.asList(fe1, fe2);

        WebExchangeBindException ex = Mockito.mock(WebExchangeBindException.class);
        Mockito.when(ex.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponse> resp = handler.handleValidationException(ex).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getMessage()).contains("Validation failed");
        assertThat(body.getMessage()).contains("name: must not be empty");
        assertThat(body.getDetails()).isInstanceOf(Map.class);
        Map<String, Object> details = (Map<String, Object>) body.getDetails();
        assertThat(details).containsEntry("name", "must not be empty");
        assertThat(details).containsEntry("amount", "must be positive");
    }

    @Test
    void handleIllegalArgument_withMessage_returnsBadRequestWithMessage() {
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(new IllegalArgumentException("bad input")).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("BAD_REQUEST");
        assertThat(body.getMessage()).isEqualTo("bad input");
    }

    @Test
    void handleIllegalArgument_withoutMessage_returnsDefaultBadRequest() {
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(new IllegalArgumentException()).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("BAD_REQUEST");
        assertThat(body.getMessage()).isEqualTo("Bad request");
    }

    @Test
    void handleGenericException_withMessage_includesMessage() {
        ResponseEntity<ErrorResponse> resp = handler.handleGenericException(new Exception("boom")).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getMessage()).contains("Internal server error: boom");
    }

    @Test
    void handleGenericException_withoutMessage_omitsColon() {
        ResponseEntity<ErrorResponse> resp = handler.handleGenericException(new Exception(" ")).block();
        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getMessage()).isEqualTo("Internal server error");
    }
}