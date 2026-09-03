package com.davivienda.global.invoice.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsBusinessToBadRequest() {
        assertThat(handler.handleBusiness(new BusinessException("x")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsNotFound() {
        assertThat(handler.handleNotFound(new ResourceNotFoundException("x")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void mapsSoap() {
        assertThat(handler.handleSoap(new SoapIntegrationException("soap")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void mapsBadCredentials() {
        assertThat(handler.handleBadCredentials(new BadCredentialsException("bad")).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void mapsIllegalArgument() {
        assertThat(handler.handleIllegalArgument(new IllegalArgumentException("no")).getBody())
                .containsEntry("error", "no");
    }

    @Test
    void mapsValidation() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("req", "subtotal", "must be positive")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        assertThat(handler.handleValidation(ex).getBody().get("error")).contains("subtotal");
    }
}
