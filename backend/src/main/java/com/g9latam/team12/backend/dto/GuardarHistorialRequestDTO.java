package com.g9latam.team12.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuardarHistorialRequestDTO(
        @NotNull @Min(0) Integer consumoKwh,
        @NotBlank String categoria,
        @NotNull Double probabilidad,
        @NotNull Double costoEstimadoMensual
) {}