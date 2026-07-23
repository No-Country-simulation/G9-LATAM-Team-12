package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.service.ModeloPredictor;
import jakarta.validation.Valid;
import  org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analisis-energetico")
public class AnalisisEnergeticoController {

    private final ModeloPredictor modeloPredictor;

    public AnalisisEnergeticoController(ModeloPredictor modeloPredictor) {
        this.modeloPredictor = modeloPredictor;
    }

    @PostMapping
    public ResponseEntity<AnalisisResponseDTO> analizar(@Valid @RequestBody ConsumoRequestDTO request) {
        AnalisisResponseDTO response = modeloPredictor.predecir(request);
        return ResponseEntity.ok(response);
    }
}