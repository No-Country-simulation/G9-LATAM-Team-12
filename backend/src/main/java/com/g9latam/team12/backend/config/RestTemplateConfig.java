package com.g9latam.team12.backend.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    // Cliente HTTP para llamar al modelo de ML. Timeouts evitan hilos colgados.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(3000)) // tiempo de conexión
                .readTimeout(Duration.ofMillis(5000)) // tiempo de respuesta
                .build();
    }
}


