package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.payload.request.LoginRequest;
import com.gamesmicroservice.rest.payload.request.RegisterRequest;
import com.gamesmicroservice.rest.payload.response.JwtResponse;
import com.gamesmicroservice.rest.payload.response.MessageResponse;
import com.gamesmicroservice.rest.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("user", "password");
        JwtResponse mockResponse = new JwtResponse("token", 1L, "user", "user@example.com", null);

        when(authService.authenticateUser(request)).thenReturn(mockResponse);

        ResponseEntity<JwtResponse> response = authController.authenticateUser(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("token", response.getBody().getToken());
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("user", "user@example.com", "password");
        MessageResponse message = new MessageResponse("User registered successfully!");

        when(authService.registerUser(request)).thenReturn(message);

        ResponseEntity<MessageResponse> response = authController.registerUser(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully!", response.getBody().getMessage());
    }
}
