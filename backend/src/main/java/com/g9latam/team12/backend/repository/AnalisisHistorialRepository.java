package com.g9latam.team12.backend.repository;

import com.g9latam.team12.backend.model.AnalisisHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalisisHistorialRepository extends JpaRepository<AnalisisHistorial, Long> {

    List<AnalisisHistorial> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}