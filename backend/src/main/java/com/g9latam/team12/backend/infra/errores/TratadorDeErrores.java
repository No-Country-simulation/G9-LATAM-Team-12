package com.g9latam.team12.backend.infra.errores;

import com.g9latam.team12.backend.dto.ErrorRespuestaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErrores {

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

    private record DatosErrorValidacion(String campo, String mensaje, String codigo) {
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage(), "CAMPO_INVALIDO");
        }
    }
}
