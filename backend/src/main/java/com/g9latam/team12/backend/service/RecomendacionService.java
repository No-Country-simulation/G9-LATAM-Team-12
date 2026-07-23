package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RecomendacionService {

    public List<String> generarRecomendaciones(String categoria, ConsumoRequestDTO request) {
        List<String> recomendaciones = new ArrayList<>();

        // Regla IIEA #1: Ineficiente + horario pico -> Peak Shaving
        if ("Ineficiente".equals(categoria) && Boolean.TRUE.equals(request.usoHorarioPico())) {
            recomendaciones.add(
                    "Desplazá actividades pesadas (lavarropas, secadoras, climatización) " +
                            "fuera del horario pico para reducir el costo de la factura."
            );
        }

        // Regla IIEA #2: Ineficiente + fuera de horario pico -> Vampire Load
        if ("Ineficiente".equals(categoria) && Boolean.FALSE.equals(request.usoHorarioPico())) {
            recomendaciones.add(
                    "El problema es el consumo continuo: eliminá el modo stand-by de " +
                            "aparatos y verificá el aislamiento térmico de tu hogar."
            );
        }

        // Regla IIEA #3: Moderado + muchas horas de alto consumo -> alerta preventiva
        if ("Moderado".equals(categoria)
                && request.horasAltoConsumo() != null
                && request.horasAltoConsumo() > 4) {
            recomendaciones.add(
                    "Agrupá el uso de equipos de alto consumo en bloques más cortos " +
                            "para mantenerte en la franja eficiente."
            );
        }

        // Regla PROCEL #1: fuera de horario pico + muchos equipos -> Selo Procel
        if (Boolean.FALSE.equals(request.usoHorarioPico())
                && request.cantidadEquipos() != null
                && request.cantidadEquipos() > 8) {
            recomendaciones.add(
                    "Alto volumen de equipos en uso continuo. Al renovar aparatos 24/7 " +
                            "(heladeras/freezers), priorizá modelos con Selo Procel (Clase A)."
            );
        }

        // Regla PROCEL #2: Casa + muchas horas de alto consumo -> aislamiento térmico
        if ("Casa".equalsIgnoreCase(request.tipoInmueble())
                && request.horasAltoConsumo() != null
                && request.horasAltoConsumo() > 6) {
            recomendaciones.add(
                    "Para casas con uso continuo, optimizá el aislamiento térmico en " +
                            "techos/ventanas y ajustá el aire acondicionado a 24°C."
            );
        }

        // Fallback: si ninguna regla específica aplicó, dar un mensaje genérico por categoría
        if (recomendaciones.isEmpty()) {
            recomendaciones.add(mensajeGenerico(categoria));
        }

        return recomendaciones;
    }

    private String mensajeGenerico(String categoria) {
        return switch (categoria) {
            case "Eficiente" -> "Mantené los hábitos actuales de consumo. " +
                    "Revisá periódicamente el estado de los equipos para conservar la eficiencia.";
            case "Moderado" -> "Considerá equipos más eficientes para reducir el consumo " +
                    "y monitoreá el consumo mensual para evitar picos.";
            case "Ineficiente" -> "Evaluá aparatos con alto consumo energético y " +
                    "distribuí actividades de mayor consumo a lo largo del día.";
            default -> "No se encontraron recomendaciones para la categoría indicada.";
        };
    }
}
