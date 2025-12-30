package com.ega.bank.ega_bank_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    void generateAndParse_withSingleSecret() {
        StandardEnvironment env = new StandardEnvironment();
        JwtUtil util = new JwtUtil(env);
        // set secret via setter (simulate property/env)
        util.setJwtSecret("my-very-secret-key-which-is-long-enough-to-be-used");
        util.setValidityMillis(60_000);
        util.init();

        String token = util.generateToken("alice", List.of("USER"));
        assertNotNull(token);

        Jws<io.jsonwebtoken.Claims> parsed = util.parseToken(token);
        Claims claims = parsed.getBody();
        assertEquals("alice", claims.getSubject());
        assertTrue(claims.containsKey("roles"));
    }

    @Test
    void parse_withRotation_acceptsTokenSignedByFirstKey() {
        StandardEnvironment env = new StandardEnvironment();
        JwtUtil util = new JwtUtil(env);
        // two secrets: first used to sign, second older but accepted
        util.setJwtSecret("first-secret-key-which-is-long-enough,old-secret-key-also-long");
        util.setValidityMillis(60_000);
        util.init();

        String token = util.generateToken("bob", List.of("USER"));
        assertNotNull(token);

        // new instance simulating rotation where signer uses first, server accepts both
        JwtUtil verifier = new JwtUtil(env);
        verifier.setJwtSecret("first-secret-key-which-is-long-enough,old-secret-key-also-long");
        verifier.setValidityMillis(60_000);
        verifier.init();

        Jws<io.jsonwebtoken.Claims> parsed = verifier.parseToken(token);
        assertEquals("bob", parsed.getBody().getSubject());
    }

    @Test
    void parse_failsWithUnknownSecret() {
        StandardEnvironment env = new StandardEnvironment();
        JwtUtil util = new JwtUtil(env);
        util.setJwtSecret("secret-one-that-will-sign");
        util.setValidityMillis(60_000);
        util.init();

        String token = util.generateToken("carl", List.of("USER"));
        assertNotNull(token);

        JwtUtil verifier = new JwtUtil(env);
        verifier.setJwtSecret("different-secret");
        verifier.setValidityMillis(60_000);
        verifier.init();

        assertThrows(io.jsonwebtoken.JwtException.class, () -> verifier.parseToken(token));
    }
}
