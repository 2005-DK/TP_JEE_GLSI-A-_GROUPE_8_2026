package com.ega.bank.ega_bank_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long validityMillis;

    private Key key;

    @PostConstruct
    public void init() {
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                // derive a 256-bit key deterministically from provided secret to avoid
                // Keys.hmacShaKeyFor errors on short secrets while keeping user secret meaningful
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    keyBytes = md.digest(keyBytes);
                    log.warn("Provided JWT secret was shorter than recommended; deriving 256-bit key via SHA-256.");
                } catch (NoSuchAlgorithmException e) {
                    log.warn("SHA-256 not available, falling back to generated key", e);
                    key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                    return;
                }
            }
            key = Keys.hmacShaKeyFor(keyBytes);
        } else {
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.info("No jwt.secret provided — generated ephemeral key for development/testing.");
        }
    }

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("roles", roles))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMillis))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

}
