package com.g9latam.team12.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostoService {

    private final double tarifaReferenciaKwh;

    public CostoService(@Value("${energiai.tarifa-referencia-kwh}") double tarifaReferenciaKwh) {
        this.tarifaReferenciaKwh = tarifaReferenciaKwh;
    }

    public Double calcularCostoMensual(double consumoKwh) {
        BigDecimal costo = BigDecimal.valueOf(consumoKwh)
                .multiply(BigDecimal.valueOf(tarifaReferenciaKwh))
                .setScale(2, RoundingMode.HALF_UP);

        return costo.doubleValue();
    }
}
