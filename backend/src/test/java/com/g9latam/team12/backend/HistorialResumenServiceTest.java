package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.ResumenHistorialDTO;
import com.g9latam.team12.backend.model.AnalisisHistorial;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistorialResumenServiceTest {

    private final HistorialResumenService service = new HistorialResumenService();

    private AnalisisHistorial crear(String categoria, BigDecimal costo, LocalDateTime fecha) {
        AnalisisHistorial h = new AnalisisHistorial();
        h.setCategoria(categoria);
        h.setCostoEstimadoMensual(costo);
        h.setFecha(fecha);
        return h;
    }

    @Test
    void listaVacia_devuelveResumenEnCero() {
        ResumenHistorialDTO resumen = service.calcular(List.of());

        assertThat(resumen.promedioCostoMensual()).isEqualByComparingTo("0");
        assertThat(resumen.categoriaMasFrecuente()).isNull();
        assertThat(resumen.tendencia()).isEqualTo("ESTABLE");
    }

    @Test
    void unSoloRegistro_tendenciaEstable() {
        List<AnalisisHistorial> lista = List.of(
                crear("Moderado", new BigDecimal("200.00"), LocalDateTime.now())
        );

        ResumenHistorialDTO resumen = service.calcular(lista);

        assertThat(resumen.promedioCostoMensual()).isEqualByComparingTo("200.00");
        assertThat(resumen.categoriaMasFrecuente()).isEqualTo("Moderado");
        assertThat(resumen.tendencia()).isEqualTo("ESTABLE");
    }

    @Test
    void calculaPromedioCorrectamente() {
        List<AnalisisHistorial> lista = List.of(
                crear("Eficiente", new BigDecimal("100.00"), LocalDateTime.now().minusDays(2)),
                crear("Moderado", new BigDecimal("200.00"), LocalDateTime.now().minusDays(1)),
                crear("Ineficiente", new BigDecimal("300.00"), LocalDateTime.now())
        );

        ResumenHistorialDTO resumen = service.calcular(lista);

        assertThat(resumen.promedioCostoMensual()).isEqualByComparingTo("200.00");
    }

    @Test
    void categoriaMasFrecuente_devuelveLaQueMasSeRepite() {
        List<AnalisisHistorial> lista = List.of(
                crear("Ineficiente", new BigDecimal("300.00"), LocalDateTime.now().minusDays(2)),
                crear("Ineficiente", new BigDecimal("310.00"), LocalDateTime.now().minusDays(1)),
                crear("Eficiente", new BigDecimal("80.00"), LocalDateTime.now())
        );

        ResumenHistorialDTO resumen = service.calcular(lista);

        assertThat(resumen.categoriaMasFrecuente()).isEqualTo("Ineficiente");
    }

    @Test
    void costoBajandoEnElTiempo_tendenciaMejorando() {
        // Ordenado de más reciente a más viejo, como viene del repository.
        // Mitad vieja (índices 2,3): promedio 300. Mitad reciente (índices 0,1): promedio 100.
        List<AnalisisHistorial> lista = List.of(
                crear("Eficiente", new BigDecimal("90.00"), LocalDateTime.now()),
                crear("Eficiente", new BigDecimal("110.00"), LocalDateTime.now().minusDays(1)),
                crear("Ineficiente", new BigDecimal("310.00"), LocalDateTime.now().minusDays(2)),
                crear("Ineficiente", new BigDecimal("290.00"), LocalDateTime.now().minusDays(3))
        );

        ResumenHistorialDTO resumen = service.calcular(lista);

        assertThat(resumen.tendencia()).isEqualTo("MEJORANDO");
    }

    @Test
    void costoSubiendoEnElTiempo_tendenciaEmpeorando() {
        List<AnalisisHistorial> lista = List.of(
                crear("Ineficiente", new BigDecimal("310.00"), LocalDateTime.now()),
                crear("Ineficiente", new BigDecimal("290.00"), LocalDateTime.now().minusDays(1)),
                crear("Eficiente", new BigDecimal("90.00"), LocalDateTime.now().minusDays(2)),
                crear("Eficiente", new BigDecimal("110.00"), LocalDateTime.now().minusDays(3))
        );

        ResumenHistorialDTO resumen = service.calcular(lista);

        assertThat(resumen.tendencia()).isEqualTo("EMPEORANDO");
    }
}