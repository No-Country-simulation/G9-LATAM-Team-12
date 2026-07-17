package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;

public interface ModeloPredictor {
    AnalisisResponseDTO predecir(ConsumoRequestDTO request);

}
