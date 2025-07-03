package com.gamesmicroservice.rest.controller;

import com.gamesmicroservice.rest.model.User;
import com.gamesmicroservice.rest.payload.request.UserCreateRequest;
import com.gamesmicroservice.rest.payload.response.MessageResponse;
import com.gamesmicroservice.rest.repository.UserRepository;
import com.gamesmicroservice.rest.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.http.ResponseEntity;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Collections;

public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    public UserControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User mockUser = new User();
        mockUser.setUsername("admin");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(mockUser));

        ResponseEntity<List<User>> response = userController.getAllUsers();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("password");

        MessageResponse expected = new MessageResponse("User created successfully!");
        when(authService.createUser(request)).thenReturn(expected);

        ResponseEntity<MessageResponse> response = userController.createUser(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User created successfully!", response.getBody().getMessage());
    }
}
