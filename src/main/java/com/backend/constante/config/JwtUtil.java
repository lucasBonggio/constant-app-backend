package com.backend.constante.config;


import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expitarionTime = 86400000;

    public JwtUtil() {
        byte[] keyBytes = new byte[64];
        new SecureRandom().nextBytes(keyBytes);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }    
        
    public String generateToken(String email) {
    
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expitarionTime))
                .signWith(key)
                .compact();
    
                return token;
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        final String emailFromToken = getEmailFromToken(token);
        return (emailFromToken.equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
    
        private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }   
}