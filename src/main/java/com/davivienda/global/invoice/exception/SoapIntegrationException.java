package com.davivienda.global.invoice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class SoapIntegrationException extends RuntimeException {

    public SoapIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoapIntegrationException(String message) {
        super(message);
    }
}
