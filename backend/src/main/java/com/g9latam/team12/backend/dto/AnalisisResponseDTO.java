package com.g9latam.team12.backend.dto;

import java.util.List;

public record AnalisisResponseDTO(
        // Clasificación del consumo: Eficiente, Moderado, Ineficiente
        String categoria,
        // Nivel de confianza, ej. 0.87 = 87%
        Double probabilidad,
        // Recomendaciones de optimización según la categoría
        List<String> recomendaciones,
        Double costoEstimadoMensual
) {}
