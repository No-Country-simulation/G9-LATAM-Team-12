package com.g9latam.team12.backend.dto;

import java.util.List;

public record ImportarCsvResponseDTO(
        Integer filasProcesadas,
        Integer filasExitosas,
        Integer filasConError,
        List<FilaErrorDTO> errores
) {}