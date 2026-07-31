package com.g9latam.team12.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO de entrada. Representa el JSON que envía el cliente
 * (frontend o Postman) hacia el endpoint:
 * <p>
 * POST /analisis-energetico
 */
@Schema(description = "Datos de entrada del consumo eléctrico para realizar la evaluación")
public record ConsumoRequestDTO(
        @Schema(
                description = "Consumo total registrado en kilovatios-hora (kWh)",
                example = "350.5"
        )
        @NotNull(message = "El consumo en kWh es obligatorio")
        @Positive(message = "El consumo en kWh debe ser mayor a 0")
        @JsonProperty("consumo_kwh")
        Double consumoKwh,

        @Schema(
                description = "Indica si el mayor uso eléctrico se realiza en horario de tarifa pico",
                example = "true"
        )
        @NotNull(message = "Debe indicar si hay uso en horario pico")
        @JsonProperty("uso_horario_pico")
        Boolean usoHorarioPico,

        @Schema(
                description = "Cantidad total de electrodomésticos o equipos de alto consumo",
                example = "3"
        )
        @NotNull(message = "La cantidad de equipos es obligatoria")
        @Min(value = 1, message = "La cantidad de equipos debe ser al menos 1")
        @JsonProperty("cantidad_equipos")
        Integer cantidadEquipos,

        @Schema(
                description = "Tipo o clasificación del inmueble",
                example = "Casa",
                allowableValues = {"Casa", "Departamento"}
        )
        @NotBlank(message = "El tipo de inmueble es obligatorio")
        @JsonProperty("tipo_inmueble")
        String tipoInmueble,

        @Schema(
                description = "Promedio de horas al día de uso intensivo de electricidad (0 a 24)",
                example = "8.5"
        )
        @NotNull(message = "Las horas de alto consumo son obligatorias")
        @Min(value = 0, message = "Las horas de alto consumo no pueden ser negativas")
        @Max(value = 24, message = "Las horas de alto consumo no pueden superar 24")
        @JsonProperty("horas_alto_consumo")
        Double horasAltoConsumo

) {}
