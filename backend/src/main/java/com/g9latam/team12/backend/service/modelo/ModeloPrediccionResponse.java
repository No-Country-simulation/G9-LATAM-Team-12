package com.g9latam.team12.backend.service.modelo;

// Respuesta del modelo de ML: categoría predicha + probabilidad
public record ModeloPrediccionResponse(String categoria, Double probabilidad) {
}
