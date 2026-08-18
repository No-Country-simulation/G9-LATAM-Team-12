package com.g9latam.team12.backend.dto;

import java.math.BigDecimal;

public record ResumenHistorialDTO(
        BigDecimal promedioCostoMensual,
        String categoriaMasFrecuente,
        String tendencia
) {}