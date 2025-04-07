package ru.bicev.notes.service;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public JwtService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpiration)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            logger.warn("Invalid token: {} Exception: {}", token, e);
            throw new IllegalArgumentException("Invalid token", e);
        }

    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            logger.warn("Token expired: {} Exception: {}", token, e);
            return true;
        }

    }

}
