package com.g9latam.team12.backend;

import com.g9latam.team12.backend.service.CostoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CostoServiceTest {
    private final CostoService costoService = new CostoService(0.75);

    @Test
    void calculaCostoSegunEjemploDelEnunciado() {
        // Ejemplo exacto del PDF: 420 kWh -> 315.00
        Double costo = costoService.calcularCostoMensual(420);

        assertEquals(315.00, costo);
    }

    @Test
    void redondeaCorrectamenteADosDecimales() {
        // 333.33 kWh * 0.75 = 249.9975 -> debe redondear a 250.00
        Double costo = costoService.calcularCostoMensual(333.33);

        assertEquals(250.00, costo);
    }

    @Test
    void consumoCeroDevuelveCostoCero() {
        Double costo = costoService.calcularCostoMensual(0);

        assertEquals(0.00, costo);
    }

    @Test
    void consumoBajoCalculaProporcionalmente() {
        // 100 kWh * 0.75 = 75.00
        Double costo = costoService.calcularCostoMensual(100);

        assertEquals(75.00, costo);
    }

}
