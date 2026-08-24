package com.g9latam.team12.backend.infra.errores;

import com.g9latam.team12.backend.dto.ErrorRespuestaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// Indica que esta clase manejará excepciones de forma global
// para TODOS los controladores de la aplicación
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // Manejo de errores de validación (cuando usas @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
        var detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DatosErrorValidacion::new)
                .toList();

        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                detalles
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    // Manejo de errores de negocio (excepciones personalizadas)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleBusinessError(BusinessException ex) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Error de negocio",
                List.of(ex.getMessage())
        );

        return ResponseEntity.unprocessableEntity().body(respuesta);
    }

    // Manejo de errores por argumentos inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleIllegalArgument(IllegalArgumentException ex) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro inválido",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(respuesta);
    }


    private record DatosErrorValidacion(String campo, String mensaje, String codigo) {
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage(), "CAMPO_INVALIDO");
        }
    }
    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleEmailYaRegistrado(EmailYaRegistradoException ex) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleAuthenticationError(
            org.springframework.security.core.AuthenticationException ex) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales inválidas",
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> handleGeneralError(Exception ex) {
        log.error("Error interno no controlado", ex);

        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                null
        );

        return ResponseEntity.internalServerError().body(respuesta);
    }
}