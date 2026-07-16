package com.g9latam.team12.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * DTO de entrada. Representa el JSON que envía el cliente
 * (frontend o Postman) hacia el endpoint:
 * <p>
 * POST /analisis-energetico
 */
public record ConsumoRequestDTO(

        @NotNull(message = "El consumo en kWh es obligatorio")
        @Positive(message = "El consumo en kWh debe ser mayor a 0")
        @JsonProperty("consumo_kwh")
        Double consumoKwh,

        @NotNull(message = "Debe indicar si hay uso en horario pico")
        @JsonProperty("uso_horario_pico")
        Boolean usoHorarioPico,

        @NotNull(message = "La cantidad de equipos es obligatoria")
        @Min(value = 1, message = "La cantidad de equipos debe ser al menos 1")
        @JsonProperty("cantidad_equipos")
        Integer cantidadEquipos,

        @NotBlank(message = "El tipo de inmueble es obligatorio")
        @JsonProperty("tipo_inmueble")
        String tipoInmueble,

        @NotNull(message = "Las horas de alto consumo son obligatorias")
        @Min(value = 0, message = "Las horas de alto consumo no pueden ser negativas")
        @Max(value = 24, message = "Las horas de alto consumo no pueden superar 24")
        @JsonProperty("horas_alto_consumo")
        Double horasAltoConsumo

) {}
