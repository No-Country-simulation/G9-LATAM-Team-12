package com.g9latam.team12.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String email) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(ahora)
                .withExpiresAt(expiracion)
                .sign(getAlgorithm());
    }

    public String extractEmail(String token) {
        DecodedJWT decoded = JWT.require(getAlgorithm())
                .build()
                .verify(token);
        return decoded.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(getAlgorithm())
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }
}