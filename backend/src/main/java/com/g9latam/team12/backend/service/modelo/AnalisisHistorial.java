package com.g9latam.team12.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analisis_historial")
@Getter
@Setter
@NoArgsConstructor
public class AnalisisHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "consumo_kwh", nullable = false)
    private Integer consumoKwh;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Double probabilidad;

    @Column(name = "costo_estimado_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimadoMensual;

    @Column(nullable = false)
    private LocalDateTime fecha;
}