package com.eqa.eqagatewayservice.exception;

public class TokenValidationException extends RuntimeException {

    public TokenValidationException(String message) {
        super(message);
    }
}
