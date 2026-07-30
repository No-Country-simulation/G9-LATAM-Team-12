package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.service.ModeloPredictor;
import jakarta.validation.Valid;
import  org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analisis-energetico")
@CrossOrigin(origins = "http://127.0.0.1:5500")
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