package com.g9latam.team12.backend.dto;

import java.util.List;

public record ErrorRespuestaDTO(
        int status,
        String error,
        List<?> detalles
){}
