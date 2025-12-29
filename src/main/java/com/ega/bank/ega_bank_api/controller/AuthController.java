package com.ega.bank.ega_bank_api.controller;

import com.ega.bank.ega_bank_api.dto.AuthRequest;
import com.ega.bank.ega_bank_api.dto.AuthResponse;
import com.ega.bank.ega_bank_api.dto.RegisterRequest;
import com.ega.bank.ega_bank_api.model.AppUser;
import com.ega.bank.ega_bank_api.repository.AppUserRepository;
import com.ega.bank.ega_bank_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        AppUser u = new AppUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRoles(Set.of("ROLE_USER"));
        userRepository.save(u);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        var user = userRepository.findByUsername(req.getUsername()).get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles().stream().toList());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
