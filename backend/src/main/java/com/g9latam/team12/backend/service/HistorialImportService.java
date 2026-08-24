package com.g9latam.team12.backend.service;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.dto.FilaErrorDTO;
import com.g9latam.team12.backend.dto.ImportarCsvResponseDTO;
import com.g9latam.team12.backend.infra.errores.BusinessException;
import com.g9latam.team12.backend.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistorialImportService {

    private static final String[] COLUMNAS_ESPERADAS = {
            "fecha", "consumo_kwh", "uso_horario_pico", "cantidad_equipos", "tipo_inmueble", "horas_alto_consumo"
    };

    private final ModeloPredictor modeloPredictor;
    private final AnalisisHistorialService historialService;

    public HistorialImportService(ModeloPredictor modeloPredictor, AnalisisHistorialService historialService) {
        this.modeloPredictor = modeloPredictor;
        this.historialService = historialService;
    }

    public ImportarCsvResponseDTO importar(Usuario usuario, MultipartFile file) {
        List<FilaErrorDTO> errores = new ArrayList<>();
        int procesadas = 0;
        int exitosas = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            validarHeader(header);

            String linea;
            int numeroFila = 1; // fila 1 = primera línea de datos (después del header)

            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) continue;
                procesadas++;

                try {
                    procesarFila(usuario, linea);
                    exitosas++;
                } catch (Exception e) {
                    errores.add(new FilaErrorDTO(numeroFila, e.getMessage()));
                }
                numeroFila++;
            }

        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo CSV.");
        }

        return new ImportarCsvResponseDTO(procesadas, exitosas, errores.size(), errores);
    }

    private void validarHeader(String header) {
        if (header == null) {
            throw new BusinessException("El archivo CSV está vacío.");
        }
        String[] columnas = header.split(",");
        if (columnas.length != COLUMNAS_ESPERADAS.length) {
            throw new BusinessException(
                    "El encabezado del CSV debe tener las columnas: " + String.join(",", COLUMNAS_ESPERADAS));
        }
    }

    private void procesarFila(Usuario usuario, String linea) {
        String[] campos = linea.split(",", -1); // -1 conserva campos vacíos (ej. fecha en blanco)

        if (campos.length != COLUMNAS_ESPERADAS.length) {
            throw new IllegalArgumentException("Cantidad de columnas inválida (se esperaban " + COLUMNAS_ESPERADAS.length + ")");
        }

        LocalDateTime fecha = parsearFecha(campos[0].trim());

        ConsumoRequestDTO request;
        try {
            request = new ConsumoRequestDTO(
                    Double.valueOf(campos[1].trim()),
                    Boolean.valueOf(campos[2].trim()),
                    Integer.valueOf(campos[3].trim()),
                    campos[4].trim(),
                    Double.valueOf(campos[5].trim()),
                    false
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido en la fila");
        }

        if (request.tipoInmueble().isBlank()) {
            throw new IllegalArgumentException("tipo_inmueble no puede estar vacío");
        }

        AnalisisResponseDTO respuesta = modeloPredictor.predecir(request);
        historialService.guardar(usuario, request, respuesta, fecha);
    }

    private LocalDateTime parsearFecha(String valor) {
        if (valor.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDate.parse(valor).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida (formato esperado yyyy-MM-dd): " + valor);
        }
    }
}