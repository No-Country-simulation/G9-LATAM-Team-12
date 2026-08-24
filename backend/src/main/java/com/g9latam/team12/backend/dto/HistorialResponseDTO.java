package com.g9latam.team12.backend.dto;

import java.util.List;

public record HistorialResponseDTO(
        List<AnalisisHistorialItemDTO> analisis,
        ResumenHistorialDTO resumen

) {}