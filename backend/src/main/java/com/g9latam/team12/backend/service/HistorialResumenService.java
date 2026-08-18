package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.ResumenHistorialDTO;
import com.g9latam.team12.backend.model.AnalisisHistorial;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HistorialResumenService {

    public ResumenHistorialDTO calcular(List<AnalisisHistorial> historial) {
        if (historial.isEmpty()) {
            return new ResumenHistorialDTO(BigDecimal.ZERO, null, "ESTABLE");
        }

        BigDecimal promedio = calcularPromedio(historial);
        String categoriaMasFrecuente = calcularCategoriaMasFrecuente(historial);
        String tendencia = calcularTendencia(historial);

        return new ResumenHistorialDTO(promedio, categoriaMasFrecuente, tendencia);
    }

    private BigDecimal calcularPromedio(List<AnalisisHistorial> historial) {
        BigDecimal suma = historial.stream()
                .map(AnalisisHistorial::getCostoEstimadoMensual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return suma.divide(BigDecimal.valueOf(historial.size()), 2, RoundingMode.HALF_UP);
    }

    private String calcularCategoriaMasFrecuente(List<AnalisisHistorial> historial) {
        Map<String, Long> conteo = historial.stream()
                .collect(Collectors.groupingBy(AnalisisHistorial::getCategoria, Collectors.counting()));

        return conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String calcularTendencia(List<AnalisisHistorial> historial) {
        // La lista viene ordenada de más reciente a más viejo.
        if (historial.size() < 2) {
            return "ESTABLE";
        }

        int mitad = historial.size() / 2;
        List<AnalisisHistorial> masReciente = historial.subList(0, mitad);
        List<AnalisisHistorial> masVieja = historial.subList(mitad, historial.size());

        BigDecimal promedioReciente = calcularPromedio(masReciente);
        BigDecimal promedioVieja = calcularPromedio(masVieja);

        int comparacion = promedioReciente.compareTo(promedioVieja);
        if (comparacion < 0) {
            return "MEJORANDO";
        } else if (comparacion > 0) {
            return "EMPEORANDO";
        } else {
            return "ESTABLE";
        }
    }
}