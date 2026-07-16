package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import jakarta.validation.Valid;
import  org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analisis-energetico")
public class AnalisisEnergeticoController {

    @PostMapping
    public ResponseEntity<AnalisisResponseDTO> analizar(@Valid @RequestBody ConsumoRequestDTO request) {
        AnalisisResponseDTO response =
                new AnalisisResponseDTO("Ineficiente", 0.81);

        return ResponseEntity.ok(response);
    }
}