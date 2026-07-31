package com.g9latam.team12.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Estructura estándar de respuesta para errores de la API")
public record ErrorRespuestaDTO(
        @Schema(description = "Código de estado HTTP")
        int status,

        @Schema(description = "Descripción breve del tipo de error")
        String error,

        @Schema(
                description = "Lista detallada de los fallos encontrados ")
        List<?> detalles
){}
