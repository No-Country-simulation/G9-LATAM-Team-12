package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.DatosRespuestaAnalisis;
import  org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analisis-energetico")
public class AnalisisEnergeticoController {

    @PostMapping
    public ResponseEntity<DatosRespuestaAnalisis> analizar() {
        DatosRespuestaAnalisis response =
                new DatosRespuestaAnalisis("Ineficiente", 0.81);

        return ResponseEntity.ok(response);
    }
}