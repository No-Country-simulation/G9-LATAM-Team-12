package com.g9latam.team12.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Análisis Energético")
                        .version("1.0.0")
                        .description("API REST para evaluar el consumo eléctrico de inmuebles, " +
                                "clasificar la eficiencia energética y brindar recomendaciones de ahorro.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .url("https://github.com/No-Country-simulation/G9-LATAM-Team-12")));
    }
}