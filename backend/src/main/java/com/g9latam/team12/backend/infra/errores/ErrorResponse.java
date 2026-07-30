package com.g9latam.team12.backend.infra.errores;
import java.time.LocalDateTime;

// Clase que define el formato estándar de error
public class ErrorResponse {

    // Código del error (ej: 400, 422, 500)
    private String codigo;

    // Mensaje principal del error
    private String mensaje;

    // Fecha y hora del error
    private LocalDateTime timestamp;

    // Información adicional del error
    private Object detalles;

    // Constructor
    public ErrorResponse(String codigo, String mensaje, Object detalles) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now(); // se genera automáticamente
        this.detalles = detalles;
    }

    // Getters (para que Spring lo convierta a JSON)

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Object getDetalles() {
        return detalles;
    }
}
