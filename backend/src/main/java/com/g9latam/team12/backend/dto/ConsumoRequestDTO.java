package com.g9latam.team12.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de entrada. Representa el JSON que envía el cliente
 * (frontend o Postman) hacia el endpoint:
 * <p>
 * POST /analisis-energetico
 */
public record ConsumoRequestDTO(

        @JsonProperty("consumo_kwh")
        Double consumoKwh,

        @JsonProperty("uso_horario_pico")
        Boolean usoHorarioPico,

        @JsonProperty("cantidad_equipos")
        Integer cantidadEquipos,

        @JsonProperty("tipo_inmueble")
        String tipoInmueble,

        @JsonProperty("horas_alto_consumo")
        Double horasAltoConsumo

) {}
