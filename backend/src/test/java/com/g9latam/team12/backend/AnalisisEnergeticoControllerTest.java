package com.g9latam.team12.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.g9latam.team12.backend.repository.AnalisisHistorialRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración del endpoint POST /analisis-energetico.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class AnalisisEnergeticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    private String token;
    @Autowired
    private AnalisisHistorialRepository historialRepository;

    @BeforeEach
    void autenticar() throws Exception {
        String registerJson = """
                {
                  "email": "test-analisis@energiai.com",
                  "password": "password123"
                }
                """;

        // Ignoramos el resultado: si ya existe (409), no importa, solo necesitamos el login.
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        token = json.get("token").asText();
    }

    @Test
    void consumoAltoDevuelveCategoriaIneficiente() throws Exception {
        String requestJson = """
                {
                  "consumo_kwh": 450,
                  "uso_horario_pico": true,
                  "cantidad_equipos": 10,
                  "tipo_inmueble": "Casa",
                  "horas_alto_consumo": 8
                }
                """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Ineficiente"))
                .andExpect(jsonPath("$.probabilidad").value(greaterThanOrEqualTo(0.70)))
                .andExpect(jsonPath("$.probabilidad").value(lessThanOrEqualTo(0.95)));
    }

    @Test
    void consumoMedioDevuelveCategoriaModerado() throws Exception {
        String requestJson = """
                {
                  "consumo_kwh": 300,
                  "uso_horario_pico": false,
                  "cantidad_equipos": 5,
                  "tipo_inmueble": "Depto",
                  "horas_alto_consumo": 4
                }
                """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Moderado"));
    }

    @Test
    void consumoBajoDevuelveCategoriaEficiente() throws Exception {
        String requestJson = """
                {
                  "consumo_kwh": 100,
                  "uso_horario_pico": false,
                  "cantidad_equipos": 2,
                  "tipo_inmueble": "Depto",
                  "horas_alto_consumo": 1
                }
                """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Eficiente"));
    }

    @Test
    void consumoNegativoDevuelveError400() throws Exception {
        String requestJson = """
                {
                  "consumo_kwh": -50,
                  "uso_horario_pico": false,
                  "cantidad_equipos": 2,
                  "tipo_inmueble": "Depto",
                  "horas_alto_consumo": 1
                }
                """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"));
    }

    @Test
    void camposFaltantesDevuelveError400() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 300
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"));
    }

    @Test
    void horasAltoConsumoFueraDeRangoDevuelveError400() throws Exception {
        String requestJson = """
                {
                  "consumo_kwh": 300,
                  "uso_horario_pico": true,
                  "cantidad_equipos": 5,
                  "tipo_inmueble": "Casa",
                  "horas_alto_consumo": 30
                }
                """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"));
    }

    @Test
    void cantidadEquiposEnCeroDevuelveError400() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 300,
              "uso_horario_pico": true,
              "cantidad_equipos": 0,
              "tipo_inmueble": "Casa",
              "horas_alto_consumo": 5
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.detalles[0].campo").value("cantidadEquipos"));
    }

    @Test
    void ineficienteConHorarioPicoSugierePeakShaving() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 450,
              "uso_horario_pico": true,
              "cantidad_equipos": 5,
              "tipo_inmueble": "Depto",
              "horas_alto_consumo": 3
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recomendaciones[0]").value(
                        org.hamcrest.Matchers.containsString("fuera del horario pico")));
    }

    @Test
    void ineficienteSinHorarioPicoSugiereEliminarStandBy() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 450,
              "uso_horario_pico": false,
              "cantidad_equipos": 5,
              "tipo_inmueble": "Depto",
              "horas_alto_consumo": 3
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recomendaciones[0]").value(
                        org.hamcrest.Matchers.containsString("stand-by")));
    }

    @Test
    void casaConMuchasHorasSumaRecomendacionDeAislamiento() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 450,
              "uso_horario_pico": true,
              "cantidad_equipos": 5,
              "tipo_inmueble": "Casa",
              "horas_alto_consumo": 8
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recomendaciones.length()").value(2));
    }

    @Test
    void ejemploCompletoDelEnunciadoDevuelveCostoEstimadoCorrecto() throws Exception {
        String requestJson = """
            {
              "consumo_kwh": 420,
              "uso_horario_pico": true,
              "cantidad_equipos": 10,
              "tipo_inmueble": "Casa",
              "horas_alto_consumo": 8
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costo_estimado_mensual").value(315.00));
    }
    @Test
    void conGuardarTrue_persisteEnElHistorial() throws Exception {
        long cantidadAntes = historialRepository.count();

        String requestJson = """
            {
              "consumo_kwh": 300,
              "uso_horario_pico": false,
              "cantidad_equipos": 5,
              "tipo_inmueble": "Depto",
              "horas_alto_consumo": 4,
              "guardar": true
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        assertThat(historialRepository.count()).isEqualTo(cantidadAntes + 1);
    }

    @Test
    void conGuardarFalse_noPersisteNada() throws Exception {
        long cantidadAntes = historialRepository.count();

        String requestJson = """
            {
              "consumo_kwh": 300,
              "uso_horario_pico": false,
              "cantidad_equipos": 5,
              "tipo_inmueble": "Depto",
              "horas_alto_consumo": 4,
              "guardar": false
            }
            """;

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        assertThat(historialRepository.count()).isEqualTo(cantidadAntes);
    }
}