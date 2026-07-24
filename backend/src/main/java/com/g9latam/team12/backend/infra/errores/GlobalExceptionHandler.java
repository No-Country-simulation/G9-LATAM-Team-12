package com.g9latam.team12.backend.infra.errores;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

// Indica que esta clase manejará excepciones de forma global
// para TODOS los controladores de la aplicación
@ControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de errores de validación (cuando usas @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {

        // Se recorren todos los errores de los campos
        // y se construye un string con el formato: campo: mensaje
        String detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // Se crea la respuesta de error usando tu clase estándar
        ErrorResponse error = new ErrorResponse(
                "400",                // Código de error
                "Datos inválidos",   // Mensaje general
                detalles             // Detalles específicos de validación
        );

        // Se devuelve el error con HTTP 400 (Bad Request)
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    // Manejo de errores de negocio (excepciones personalizadas)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessError(BusinessException ex) {

        // Se crea el error con el mensaje de la excepción
        ErrorResponse error = new ErrorResponse(
                "422",                    // Código semántico (Unprocessable Entity)
                ex.getMessage(),         // Mensaje personalizado
                "Error de negocio"       // Descripción adicional
        );

        // Se devuelve HTTP 422
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    // Manejo de errores por argumentos inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {

        // Se construye el error con el mensaje recibido
        ErrorResponse error = new ErrorResponse(
                "400",
                ex.getMessage(),
                "Parámetro inválido"
        );

        // Se devuelve HTTP 400
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    //  Manejo de errores generales (cualquier excepción no controlada)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {

        //no exponer detalles internos (seguridad)
        ErrorResponse error = new ErrorResponse(
                "500",
                "Error interno del servidor",
                null
        );

        // Se devuelve HTTP 500
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
