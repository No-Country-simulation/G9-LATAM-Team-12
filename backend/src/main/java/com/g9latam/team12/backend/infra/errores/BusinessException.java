package com.g9latam.team12.backend.infra.errores;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
