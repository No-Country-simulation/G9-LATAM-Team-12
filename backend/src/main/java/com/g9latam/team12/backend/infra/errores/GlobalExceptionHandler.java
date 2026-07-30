package com.g9latam.team12.backend.infra.errores;

import com.g9latam.team12.backend.dto.ErrorRespuestaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDTO> tratarError400(MethodArgumentNotValidException e) {
        var detalles = e.getFieldErrors().stream()
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
    public ResponseEntity<ErrorRespuestaDTO> tratarErrorDeNegocio(BusinessException e) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Error de negocio",
                List.of(e.getMessage())
        );

        return ResponseEntity.unprocessableEntity().body(respuesta);
    }

    // Manejo de errores por argumentos inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDTO> tratarArgumentoInvalido(IllegalArgumentException e) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro inválido",
                List.of(e.getMessage())
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    // Manejo de errores generales (cualquier excepción no controlada)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> tratarErrorGeneral(Exception e) {
        var respuesta = new ErrorRespuestaDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                null // no exponer detalles internos por seguridad
        );

        return ResponseEntity.internalServerError().body(respuesta);
    }

    private record DatosErrorValidacion(String campo, String mensaje, String codigo) {
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage(), "CAMPO_INVALIDO");
        }
    }
}
