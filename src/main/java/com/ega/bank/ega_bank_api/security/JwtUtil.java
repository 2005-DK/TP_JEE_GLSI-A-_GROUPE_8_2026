package com.ega.bank.ega_bank_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

    private final Environment env;

    @Value("${jwt.expiration-ms:86400000}")
    private long validityMillis;

    private Key key;
    // support multiple keys (rotation): first is used to sign, others accepted for parsing
    private Key[] acceptedKeys;

    public JwtUtil(Environment env) {
        this.env = env;
    }

    // For testing convenience: allow setting the raw secret programmatically
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    // For testing: configure token validity
    public void setValidityMillis(long validityMillis) {
        this.validityMillis = validityMillis;
    }

    @PostConstruct
    public void init() {
        // Priority: environment variable JWT_SECRET (can be comma-separated for rotation), then property jwt.secret
        String envSecret = env.getProperty("JWT_SECRET");
        String raw = envSecret != null && !envSecret.isBlank() ? envSecret : jwtSecret;
        if (raw != null && !raw.isBlank()) {
            String[] parts = raw.split(",");
            acceptedKeys = new Key[parts.length];
            for (int i = 0; i < parts.length; i++) {
                byte[] keyBytes = parts[i].trim().getBytes(StandardCharsets.UTF_8);
                if (keyBytes.length < 32) {
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        keyBytes = md.digest(keyBytes);
                        log.warn("Provided JWT secret was shorter than recommended; deriving 256-bit key via SHA-256.");
                    } catch (NoSuchAlgorithmException e) {
                        log.warn("SHA-256 not available, falling back to generated key", e);
                        acceptedKeys[i] = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                        continue;
                    }
                }
                acceptedKeys[i] = Keys.hmacShaKeyFor(keyBytes);
            }
            // first key used for signing
            key = acceptedKeys[0];
        } else {
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            acceptedKeys = new Key[]{key};
            log.info("No jwt.secret or JWT_SECRET provided — generated ephemeral key for development/testing.");
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
        // Try all accepted keys to support rotation: first successful parse is returned
        JwtException lastEx = null;
        for (Key k : acceptedKeys) {
            try {
                return Jwts.parserBuilder().setSigningKey(k).build().parseClaimsJws(token);
            } catch (JwtException ex) {
                lastEx = ex;
            }
        }
        throw lastEx != null ? lastEx : new JwtException("Unable to parse token");
    }

}
