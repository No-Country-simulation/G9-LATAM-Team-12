package com.g9latam.team12.backend.infra.errores;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException(String email) {
        super("El email ya está registrado.");
    }
}