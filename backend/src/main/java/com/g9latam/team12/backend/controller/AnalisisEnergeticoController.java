package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.dto.ErrorRespuestaDTO;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import com.g9latam.team12.backend.service.AnalisisHistorialService;
import com.g9latam.team12.backend.service.ModeloPredictor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import  org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analisis-energetico")
@CrossOrigin(origins = {"http://localhost", "http://127.0.0.1:5500","http://150.136.167.160:8080"})@Tag(name = "Análisis Energético", description = "Endpoints para la evaluación de consumo eléctrico y predicción de eficiencia")
public class AnalisisEnergeticoController implements AnalisisEnergeticoApi {

    private final ModeloPredictor modeloPredictor;
    private final UsuarioRepository usuarioRepository;
    private final AnalisisHistorialService historialService;

    public AnalisisEnergeticoController(ModeloPredictor modeloPredictor,
                                        UsuarioRepository usuarioRepository,
                                        AnalisisHistorialService historialService) {
        this.modeloPredictor = modeloPredictor;
        this.usuarioRepository = usuarioRepository;
        this.historialService = historialService;
    }

    @Override
    @PostMapping
    public ResponseEntity<AnalisisResponseDTO> analizar(@Valid @RequestBody ConsumoRequestDTO request,
                                                        Authentication authentication) {
        AnalisisResponseDTO response = modeloPredictor.predecir(request);

        if (Boolean.TRUE.equals(request.guardar())) {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException(
                            "Usuario autenticado no encontrado: " + authentication.getName()));
            historialService.guardar(usuario, request, response);
        }

        return ResponseEntity.ok(response);
    }
}
