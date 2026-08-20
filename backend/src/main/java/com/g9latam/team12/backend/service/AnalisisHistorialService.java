package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.dto.GuardarHistorialRequestDTO;
import com.g9latam.team12.backend.model.AnalisisHistorial;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.AnalisisHistorialRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AnalisisHistorialService {

    private final AnalisisHistorialRepository historialRepository;

    public AnalisisHistorialService(AnalisisHistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public void guardar(Usuario usuario, ConsumoRequestDTO request, AnalisisResponseDTO response) {
        AnalisisHistorial historial = new AnalisisHistorial();
        historial.setUsuario(usuario);
        historial.setConsumoKwh(request.consumoKwh().intValue());
        historial.setCategoria(response.categoria());
        historial.setProbabilidad(response.probabilidad());
        historial.setCostoEstimadoMensual(BigDecimal.valueOf(response.costoEstimadoMensual()));
        historial.setFecha(LocalDateTime.now());

        historialRepository.save(historial);
    }

    public void guardar(Usuario usuario, GuardarHistorialRequestDTO request) {
        AnalisisHistorial historial = new AnalisisHistorial();
        historial.setUsuario(usuario);
        historial.setConsumoKwh(request.consumoKwh());
        historial.setCategoria(request.categoria());
        historial.setProbabilidad(request.probabilidad());
        historial.setCostoEstimadoMensual(BigDecimal.valueOf(request.costoEstimadoMensual()));
        historial.setFecha(LocalDateTime.now());

        historialRepository.save(historial);
    }
}