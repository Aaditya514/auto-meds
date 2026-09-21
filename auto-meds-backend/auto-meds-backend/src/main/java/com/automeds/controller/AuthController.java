package com.automeds.controller;

import com.automeds.dto.AuthRequest;
import com.automeds.dto.AuthResponse;
import com.automeds.dto.RegisterRequest;
import com.automeds.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AuthController
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Handles POST requests at this endpoint
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerPatient(request);
        return ResponseEntity.ok(response);
    }

    // Handles POST requests at this endpoint
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
