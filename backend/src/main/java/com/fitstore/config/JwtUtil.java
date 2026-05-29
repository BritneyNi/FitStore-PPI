package com.fitstore.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${fitstore.jwt.secret}")
    private String secret;

    @Value("${fitstore.jwt.expiration}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String email, String rol) {
        return Jwts.builder()
            .setSubject(email)
            .claim("rol", rol)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extraerEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getKey()).build()
            .parseClaimsJws(token)
            .getBody().getSubject();
    }

    public String extraerRol(String token) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(getKey()).build()
            .parseClaimsJws(token)
            .getBody().get("rol");
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
