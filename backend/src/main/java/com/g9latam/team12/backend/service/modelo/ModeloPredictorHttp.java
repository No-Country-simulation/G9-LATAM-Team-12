package com.g9latam.team12.backend.service.modelo;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.infra.errores.BusinessException;
import com.g9latam.team12.backend.service.CostoService;
import com.g9latam.team12.backend.service.ModeloPredictor;
import com.g9latam.team12.backend.service.RecomendacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// Implementación real que llama al modelo de ML vía HTTP.
// Se activa solo con el perfil "http" (reemplaza al mock).
@Service
@Profile("http")
public class ModeloPredictorHttp implements ModeloPredictor {
    private final RestTemplate restTemplate;
    private final RecomendacionService recomendacionService;
    private final CostoService costoService;
    private final String modeloApiUrl;

    public ModeloPredictorHttp(RestTemplate restTemplate,
                               RecomendacionService recomendacionService,
                               CostoService costoService,
                               @Value("${modelo.api.url}") String modeloApiUrl) {
        this.restTemplate = restTemplate;
        this.recomendacionService = recomendacionService;
        this.costoService = costoService;
        this.modeloApiUrl = modeloApiUrl;
    }

    @Override
    public AnalisisResponseDTO predecir(ConsumoRequestDTO request) {

        // 1. Llama al modelo de ML
        ModeloPrediccionResponse prediccion = llamarModelo(request);

        // 2. Arma la respuesta con recomendaciones y costo
        List<String> recomendaciones = recomendacionService.generarRecomendaciones(
                prediccion.categoria(), request);
        Double costoEstimadoMensual = costoService.calcularCostoMensual(request.consumoKwh());

        return new AnalisisResponseDTO(
                prediccion.categoria(),
                prediccion.probabilidad(),
                recomendaciones,
                costoEstimadoMensual
        );
    }

    // Envía los datos al modelo y traduce errores HTTP a excepciones de negocio.
    private ModeloPrediccionResponse llamarModelo(ConsumoRequestDTO request) {
        ModeloRequestPayload payload = ModeloRequestPayload.from(request);
        try {
            ModeloPrediccionResponse response = restTemplate.postForObject(
                    modeloApiUrl , payload, ModeloPrediccionResponse.class);

            if (response == null) {
                throw new BusinessException("El servicio de modelo devolvió una respuesta vacía.");
            }
            return response;

        } catch (HttpClientErrorException e) {
            // 400/422: el modelo rechazó los datos (ej. validación de FastAPI)
            throw new BusinessException(
                    "El modelo rechazó los datos de entrada: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // Timeout o modelo caído / no alcanzable
            throw new BusinessException(
                    "No se pudo conectar con el servicio de predicción. Intente nuevamente.");
        }
    }
}
