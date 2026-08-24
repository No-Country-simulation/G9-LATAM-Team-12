package com.g9latam.team12.backend.controller;

import com.g9latam.team12.backend.dto.AnalisisHistorialItemDTO;
import com.g9latam.team12.backend.dto.GuardarHistorialRequestDTO;
import com.g9latam.team12.backend.dto.HistorialResponseDTO;
import com.g9latam.team12.backend.dto.ImportarCsvResponseDTO;
import com.g9latam.team12.backend.model.AnalisisHistorial;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.AnalisisHistorialRepository;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import com.g9latam.team12.backend.service.AnalisisHistorialService;
import com.g9latam.team12.backend.service.HistorialImportService;
import com.g9latam.team12.backend.service.HistorialResumenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/historial")
public class HistorialController {

    // 1. Agrupamos todos los servicios/repositorios inyectados arriba de todo
    private final AnalisisHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialResumenService resumenService;
    private final AnalisisHistorialService historialService;
    private final HistorialImportService importService;

    // 2. Un único constructor con los 5 parámetros
    public HistorialController(AnalisisHistorialRepository historialRepository,
                               UsuarioRepository usuarioRepository,
                               HistorialResumenService resumenService,
                               AnalisisHistorialService historialService,
                               HistorialImportService importService) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.resumenService = resumenService;
        this.historialService = historialService;
        this.importService = importService;
    }

    @GetMapping
    public ResponseEntity<HistorialResponseDTO> obtenerHistorial(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado no encontrado: " + authentication.getName()));

        List<AnalisisHistorial> historial = historialRepository
                .findByUsuarioIdOrderByFechaDesc(usuario.getId());

        List<AnalisisHistorialItemDTO> analisis = historial.stream()
                .map(this::aDTO)
                .toList();

        return ResponseEntity.ok(new HistorialResponseDTO(analisis, resumenService.calcular(historial)));
    }

    private AnalisisHistorialItemDTO aDTO(AnalisisHistorial historial) {
        return new AnalisisHistorialItemDTO(
                historial.getId(),
                historial.getConsumoKwh(),
                historial.getCategoria(),
                historial.getProbabilidad(),
                historial.getCostoEstimadoMensual(),
                historial.getFecha()
        );
    }

    @PostMapping
    public ResponseEntity<Void> guardarAnalisis(
            @RequestBody @Valid GuardarHistorialRequestDTO request,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado no encontrado: " + authentication.getName()));

        historialService.guardar(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // El nuevo endpoint de importación
    @PostMapping("/importar")
    public ResponseEntity<ImportarCsvResponseDTO> importarCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado no encontrado: " + authentication.getName()));

        ImportarCsvResponseDTO resultado = importService.importar(usuario, file);
        return ResponseEntity.ok(resultado);
    }
}