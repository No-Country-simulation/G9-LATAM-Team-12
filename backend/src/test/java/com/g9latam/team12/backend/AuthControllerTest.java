package com.g9latam.team12.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;
    @Test
    void registrar_conDatosValidos_devuelve201() throws Exception {
        String requestJson = """
                {
                  "email": "nuevo@energiai.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@energiai.com"));
    }

    @Test
    void registrar_conEmailYaExistente_devuelve409() throws Exception {
        String requestJson = """
            {
              "email": "duplicado@energiai.com",
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya está registrado."));
    }

    @Test
    void login_conCredencialesValidas_devuelveToken() throws Exception {
        registrarUsuario("login@energiai.com", "password123");

        String loginJson = """
                {
                  "email": "login@energiai.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@energiai.com"));
    }

    @Test
    void login_conPasswordIncorrecta_devuelve401() throws Exception {
        registrarUsuario("otro@energiai.com", "password123");

        String loginJson = """
                {
                  "email": "otro@energiai.com",
                  "password": "passwordIncorrecta"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegido_conTokenValido_devuelve200() throws Exception {
        registrarUsuario("flujo@energiai.com", "password123");

        String loginJson = """
                {
                  "email": "flujo@energiai.com",
                  "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = json.get("token").asText();

        String analisisJson = """
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
                        .content(analisisJson))
                .andExpect(status().isOk());
    }

    private void registrarUsuario(String email, String password) throws Exception {
        String requestJson = String.format("""
                {
                  "email": "%s",
                  "password": "%s"
                }
                """, email, password);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }
}