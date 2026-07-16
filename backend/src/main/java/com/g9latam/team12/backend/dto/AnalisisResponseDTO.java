package com.g9latam.team12.backend.dto;

public record AnalisisResponseDTO(
        // Clasificación del consumo: BAJO, MEDIO, ALTO
        String categoria,
        //Nivel de confianza, ej. 0.87 = 87%
        Double probabilidad
) {}
