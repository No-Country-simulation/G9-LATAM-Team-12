package com.g9latam.team12.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado del análisis y diagnóstico energético generado por el modelo")
public record AnalisisResponseDTO(
        @Schema(
                description = "Clasificación final del consumo energético",
                example = "MODERADO",
                allowableValues = {"EFICIENTE", "MODERADO", "INEFICIENTE"}
        )
        String categoria,

        @Schema(
                description = "Grado o nivel de confianza de la predicción del modelo (entre 0.0 y 1.0)",
                example = "0.87"
        )
        Double probabilidad,

        @Schema(
                description = "Lista de sugerencias y medidas preventivas de ahorro recomendadas",
                example = "[\"Evitar el uso de artefactos de alto consumo entre las 18:00 y las 23:00\"," +
                            " \"Sustituir luminarias por tecnología LED\"]"
        )
        List<String> recomendaciones,

        @Schema(
                description = "Estimación económica del gasto mensual proyectado en moneda local",
                example = "12500.00"
        )
        Double costoEstimadoMensual
) {}
