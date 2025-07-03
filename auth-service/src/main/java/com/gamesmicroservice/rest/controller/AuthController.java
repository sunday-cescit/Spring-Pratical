package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.payload.request.LoginRequest;
import com.gamesmicroservice.rest.payload.request.RegisterRequest;
import com.gamesmicroservice.rest.payload.response.JwtResponse;
import com.gamesmicroservice.rest.payload.response.MessageResponse;
import com.gamesmicroservice.rest.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.registerUser(registerRequest));
    }
}