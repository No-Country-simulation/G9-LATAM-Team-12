package com.g9latam.team12.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AnalisisHistorialItemDTO(
        Long id,
        Integer consumoKwh,
        String categoria,
        Double probabilidad,
        BigDecimal costoEstimadoMensual,
        LocalDateTime fecha
) {}