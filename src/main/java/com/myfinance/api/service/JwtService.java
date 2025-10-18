package com.myfinance.api.service;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // 1. Clave Secreta: DEBES cambiar esto por una cadena larga y segura.
    // Puedes generar una en https://www.allkeysgenerator.com/ (256-bit)
    private static final String SECRET_KEY = "======================MyFinanceSecretKey======================";

    // 2. Genera un token para un usuario
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // El "sujeto" del token será el email del usuario
                .issuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Caduca en 24 horas
                .signWith(getSigningKey()) // Firma el token con la clave secreta
                .compact();
    }

    // 3. Extrae el email (username) del token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 4. Valida si un token es correcto
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // --- Métodos privados de ayuda ---

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}