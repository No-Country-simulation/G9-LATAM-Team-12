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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class HistorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    private String loguearComo(String email) throws Exception {
        String credenciales = String.format("""
                {
                  "email": "%s",
                  "password": "password123"
                }
                """, email);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciales));

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private void analizarYGuardar(String token, double consumoKwh) throws Exception {
        String requestJson = String.format("""
                {
                  "consumo_kwh": %s,
                  "uso_horario_pico": false,
                  "cantidad_equipos": 3,
                  "tipo_inmueble": "Depto",
                  "horas_alto_consumo": 2,
                  "guardar": true
                }
                """, consumoKwh);

        mockMvc.perform(post("/analisis-energetico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void historial_devuelveSoloLosAnalisisDelUsuarioAutenticado() throws Exception {
        String tokenA = loguearComo("historial-a@energiai.com");
        String tokenB = loguearComo("historial-b@energiai.com");

        analizarYGuardar(tokenA, 100);
        analizarYGuardar(tokenA, 200);
        analizarYGuardar(tokenB, 500);

        mockMvc.perform(get("/historial")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisis.length()").value(2));

        mockMvc.perform(get("/historial")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisis.length()").value(1));
    }

    @Test
    void historial_ordenaDelMasRecienteAlMasViejo() throws Exception {
        String token = loguearComo("historial-orden@energiai.com");

        analizarYGuardar(token, 100); // Eficiente
        analizarYGuardar(token, 450); // Ineficiente (más reciente)

        mockMvc.perform(get("/historial")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisis[0].categoria").value("Ineficiente"))
                .andExpect(jsonPath("$.analisis[1].categoria").value("Eficiente"));
    }

    @Test
    void historial_sinToken_devuelve403() throws Exception {
        mockMvc.perform(get("/historial"))
                .andExpect(status().isForbidden());
    }

    @Test
    void historial_sinAnalisisGuardados_devuelveListaVacia() throws Exception {
        String token = loguearComo("historial-vacio@energiai.com");

        mockMvc.perform(get("/historial")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisis.length()").value(0));
    }
    @Test
    void historial_incluyeResumenConPromedioYTendencia() throws Exception {
        String token = loguearComo("historial-resumen@energiai.com");

        analizarYGuardar(token, 100); // Eficiente, costo bajo
        analizarYGuardar(token, 450); // Ineficiente, costo alto

        mockMvc.perform(get("/historial")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.promedio_costo_mensual").isNotEmpty())
                .andExpect(jsonPath("$.resumen.categoria_mas_frecuente").isNotEmpty())
                .andExpect(jsonPath("$.resumen.tendencia").isNotEmpty());
    }
}