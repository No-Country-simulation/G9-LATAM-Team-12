package com.g9latam.team12.backend.dto;

// Import necesario para mapear nombres del JSON a Java
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO (Data Transfer Object) de entrada.
 * 
 *  Este objeto representa el JSON que envía el cliente (frontend o Postman)
 * hacia nuestro backend en el endpoint:
 * 
 * POST /analisis-energetico
 * 
 *  Su única responsabilidad es transportar datos (NO lógica de negocio).
 */
public class ConsumoRequest {

    /**
     * Campo: consumoKwh
     * 
     * @JsonProperty permite mapear el nombre del JSON (snake_case)
     * al nombre del atributo en Java (camelCase)
     * 
     * JSON esperado:
     * "consumo_kwh": 300
     * 
     * Java:
     * consumoKwh = 300
     */
    @JsonProperty("consumo_kwh")
    private Double consumoKwh;

    /**
     * Campo: usoHorarioPico
     * 
     * Indica si el consumo ocurre en horario de mayor demanda energética
     * (true = sí, false = no)
     */
    @JsonProperty("uso_horario_pico")
    private Boolean usoHorarioPico;

    /**
     * Campo: cantidadEquipos
     * 
     * Número de dispositivos eléctricos activos
     * Ejemplo: aire acondicionado, heladera, TV, etc.
     */
    @JsonProperty("cantidad_equipos")
    private Integer cantidadEquipos;

    /**
     * Campo: tipoInmueble
     * 
     *  Tipo de lugar donde ocurre el consumo
     * Ejemplos:
     * - "residencial"
     * - "comercial"
     * - "industrial"
     */
    @JsonProperty("tipo_inmueble")
    private String tipoInmueble;

    /**
     * Campo: horasAltoConsumo
     * 
     * Cantidad de horas al día donde el consumo es elevado
     */
    @JsonProperty("horas_alto_consumo")
    private Double horasAltoConsumo;

    /**
     * Constructor vacío
     * 
     *  Spring Boot lo necesita para poder crear el objeto automáticamente
     * cuando recibe un JSON en el request.
     */
    public ConsumoRequest() {
    }

    // =========================
    // GETTERS (leer valores)
    // =========================

    /**
     * Obtiene el consumo en kWh
     */
    public Double getConsumoKwh() {
        return consumoKwh;
    }

    /**
     * Asigna el consumo en kWh
     */
    public void setConsumoKwh(Double consumoKwh) {
        this.consumoKwh = consumoKwh;
    }

    /**
     * Indica si hay uso en horario pico
     */
    public Boolean getUsoHorarioPico() {
        return usoHorarioPico;
    }

    /**
     * Define si hay uso en horario pico
     */
    public void setUsoHorarioPico(Boolean usoHorarioPico) {
        this.usoHorarioPico = usoHorarioPico;
    }

    /**
     * Obtiene la cantidad de equipos
     */
    public Integer getCantidadEquipos() {
        return cantidadEquipos;
    }

    /**
     * Define la cantidad de equipos
     */
    public void setCantidadEquipos(Integer cantidadEquipos) {
        this.cantidadEquipos = cantidadEquipos;
    }

    /**
     * Obtiene el tipo de inmueble
     */
    public String getTipoInmueble() {
        return tipoInmueble;
    }

    /**
     * Define el tipo de inmueble
     */
    public void setTipoInmueble(String tipoInmueble) {
        this.tipoInmueble = tipoInmueble;
    }

    /**
     * Obtiene las horas de alto consumo
     */
    public Double getHorasAltoConsumo() {
        return horasAltoConsumo;
    }

    /**
     * Define las horas de alto consumo
     */
    public void setHorasAltoConsumo(Double horasAltoConsumo) {
        this.horasAltoConsumo = horasAltoConsumo;
    }
}
