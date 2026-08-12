package com.g9latam.team12.backend.service.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;

//Payload que se envía al modelo de ML vía HTTP.
public record ModeloRequestPayload(
        // se usan "@JsonProperty" para seguridad por más que esté contemplado
        // en el application.properties
        @JsonProperty("consumo_kwh") Double consumoKwh,
        @JsonProperty("uso_horario_pico") Boolean usoHorarioPico,
        @JsonProperty("cantidad_equipos") Integer cantidadEquipos,
        @JsonProperty("tipo_inmueble") String tipoInmueble,
        @JsonProperty("horas_alto_consumo") Double horasAltoConsumo
) {
    public static ModeloRequestPayload from(ConsumoRequestDTO request) {
        return new ModeloRequestPayload(
                request.consumoKwh(),
                request.usoHorarioPico(),
                request.cantidadEquipos(),
                request.tipoInmueble(),
                request.horasAltoConsumo()
        );
    }
}


