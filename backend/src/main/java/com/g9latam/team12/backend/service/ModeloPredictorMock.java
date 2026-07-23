package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementación simulada de ModeloPredictor.
 *
 * No usa ningún modelo de Machine Learning real: clasifica con reglas
 * simples y fijas, solo para poder desarrollar y testear el resto de
 * la aplicación mientras Data Science entrega el modelo entrenado.
 */
@Service
@Profile("mock")
public class ModeloPredictorMock implements ModeloPredictor {

    private final RecomendacionService recomendacionService;
    private final CostoService costoService;

    public ModeloPredictorMock(RecomendacionService recomendacionService, CostoService costoService) {
        this.recomendacionService = recomendacionService;
        this.costoService = costoService;
    }

    @Override
    public AnalisisResponseDTO predecir(ConsumoRequestDTO request) {
        String categoria = calcularCategoria(request.consumoKwh());
        Double probabilidad = generarProbabilidadSimulada();
        List<String> recomendaciones = recomendacionService.generarRecomendaciones(categoria, request);
        Double costoEstimadoMensual = costoService.calcularCostoMensual(request.consumoKwh());

        return new AnalisisResponseDTO(categoria, probabilidad, recomendaciones, costoEstimadoMensual);
    }

    private String calcularCategoria(Double consumoKwh) {
        if (consumoKwh > 400) {
            return "Ineficiente";
        } else if (consumoKwh >= 200) {
            return "Moderado";
        } else {
            return "Eficiente";
        }
    }

    private Double generarProbabilidadSimulada() {
        double probabilidad = ThreadLocalRandom.current().nextDouble(0.70, 0.95);
        return Math.round(probabilidad * 100.0) / 100.0;
    }
}