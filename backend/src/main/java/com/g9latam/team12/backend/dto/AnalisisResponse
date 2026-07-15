package com.g9latam.team12.backend.dto;

/**
 * DTO de salida para el análisis energético.
 *
 * Este objeto representa la respuesta que el backend devuelve
 * al cliente (Frontend o Postman) después de procesar el análisis.
 *
 * Se utiliza en el endpoint:
 * POST /analisis-energetico
 */
public class AnalisisResponse {

    /**
     * Clasificación del consumo energético detectado.
     *
     * Valores posibles:
     * - BAJO
     * - MEDIO
     * - ALTO
     */
    private String categoria;


    /**
     * Nivel de confianza del análisis realizado.
     *
     * Ejemplo:
     * 0.87 representa un 87% de probabilidad.
     */
    private Double probabilidad;


    /**
     * Constructor vacío requerido por Spring Boot
     * para convertir objetos automáticamente a JSON.
     */
    public AnalisisResponse() {
    }


    /**
     * Constructor utilizado para crear una respuesta
     * con los datos del análisis directamente.
     */
    public AnalisisResponse(String categoria, Double probabilidad) {
        this.categoria = categoria;
        this.probabilidad = probabilidad;
    }


    // Métodos Getters y Setters
    // Permiten acceder y modificar los valores
    // de los atributos del objeto.

    public String getCategoria() {
        return categoria;
    }


    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


    public Double getProbabilidad() {
        return probabilidad;
    }


    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }
}
