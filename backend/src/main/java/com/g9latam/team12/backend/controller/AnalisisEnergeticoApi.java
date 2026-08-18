package com.g9latam.team12.backend.controller;


import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.dto.ErrorRespuestaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Análisis Energético", description = "Endpoints para la evaluación de consumo eléctrico y predicción de eficiencia")
public interface AnalisisEnergeticoApi {
    @Operation(
            summary = "Analizar consumo energético de un inmueble",
            description = "Procesa los datos de consumo eléctrico enviados en la petición mediante el modelo predictor " +
                    "y retorna la categoría de eficiencia, costo mensual estimado y recomendaciones."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análisis generado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnalisisResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición inválida (falló la validación de los datos de entrada)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorRespuestaDTO.class),
                            examples = @ExampleObject(
                                    name = "ErrorValidacion",
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Error de validación",
                                      "detalles": [
                                        "El consumo en kWh debe ser mayor a 0",
                                        "La cantidad de equipos debe ser al menos 1"
                                      ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Error de regla de negocio al procesar el análisis",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorRespuestaDTO.class),
                            examples = @ExampleObject(
                                    name = "ErrorNegocio",
                                    value = """
                                    {
                                      "status": 422,
                                      "error": "Error de negocio",
                                      "detalles": [
                                        "El inmueble no cumple con los requisitos mínimos para ser analizado"
                                      ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno no controlado al procesar el modelo de predicción",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorRespuestaDTO.class),
                            examples = @ExampleObject(
                                    name = "ErrorServidor",
                                    value = """
                                    {
                                      "status": 500,
                                      "error": "Error interno del servidor",
                                      "detalles": null
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<AnalisisResponseDTO> analizar(@Valid @RequestBody ConsumoRequestDTO request, Authentication authentication);

}
