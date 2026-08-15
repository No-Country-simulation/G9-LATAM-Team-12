package com.g9latam.team12.backend.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Simula lo que Spring inyectaría desde application.properties
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-para-tests-min-32-caracteres");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_generaUnTokenNoVacio() {
        String token = jwtService.generateToken("test@energiai.com");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void extractEmail_devuelveElEmailUsadoParaGenerarlo() {
        String token = jwtService.generateToken("test@energiai.com");

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("test@energiai.com");
    }

    @Test
    void isTokenValid_devuelveTrueParaTokenRecienGenerado() {
        String token = jwtService.generateToken("test@energiai.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_devuelveFalseParaTokenManipulado() {
        String token = jwtService.generateToken("test@energiai.com");
        String tokenManipulado = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tokenManipulado)).isFalse();
    }

    @Test
    void extractEmail_lanzaExcepcionSiElTokenEsInvalido() {
        assertThatThrownBy(() -> jwtService.extractEmail("token.invalido.falso"))
                .isInstanceOf(JWTVerificationException.class);
    }
}