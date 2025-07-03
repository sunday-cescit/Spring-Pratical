package com.gamesmicroservice.rest.service;

import com.gamesmicroservice.rest.model.ERole;
import com.gamesmicroservice.rest.model.Role;
import com.gamesmicroservice.rest.model.User;
import com.gamesmicroservice.rest.payload.request.LoginRequest;
import com.gamesmicroservice.rest.payload.request.RegisterRequest;
import com.gamesmicroservice.rest.payload.request.UserCreateRequest;
import com.gamesmicroservice.rest.payload.response.JwtResponse;
import com.gamesmicroservice.rest.payload.response.MessageResponse;
import com.gamesmicroservice.rest.repository.RoleRepository;
import com.gamesmicroservice.rest.repository.UserRepository;
import com.gamesmicroservice.rest.util.JwtService;

import jakarta.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    @Autowired
    private jakarta.validation.Validator validator;  // Explicitly use Jakarta Validator

    public AuthService(AuthenticationManager authenticationManager, 
                      UserRepository userRepository, 
                      RoleRepository roleRepository, 
                      PasswordEncoder encoder, 
                      JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, 
                             userDetails.getId(), 
                             userDetails.getUsername(), 
                             userDetails.getEmail(), 
                             roles);
    }

    public MessageResponse registerUser(RegisterRequest registerRequest) {
    if (userRepository.existsByUsername(registerRequest.getUsername())) {
        return new MessageResponse("Error: Username is already taken!");
    }

    if (userRepository.existsByEmail(registerRequest.getEmail())) {
        return new MessageResponse("Error: Email is already in use!");
    }

    // First create user with raw password for validation
    User user = new User(registerRequest.getUsername(), 
                        registerRequest.getEmail(),
                        registerRequest.getPassword()); // Not encoded yet

    // This will validate the raw password
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    if (!violations.isEmpty()) {
        // Handle validation errors
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<User> violation : violations) {
            sb.append(violation.getMessage()).append("; ");
        }
        return new MessageResponse("Error: " + sb.toString());
    }

    // Only encode the password after validation passes
    user.setPassword(encoder.encode(registerRequest.getPassword()));

    Set<Role> roles = new HashSet<>();
    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    roles.add(userRole);

    user.setRoles(roles);
    userRepository.save(user);

    return new MessageResponse("User registered successfully!");
}
    public MessageResponse createUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        User user = new User(userCreateRequest.getUsername(), 
                            userCreateRequest.getEmail(),
                            encoder.encode(userCreateRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        ERole defaultRole = ERole.ROLE_USER;

       validateRoleName(defaultRole.name());  // validate "ROLE_USER"

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        if (userCreateRequest.isAdmin()) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User created successfully!");
    }
    private void validateRoleName(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }

        if (!roleName.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Role must start with 'ROLE_'");
        }

        if (roleName.length() < 9 || roleName.length() > 15) {
            throw new IllegalArgumentException("Role name must be between 9 and 15 characters");
        }
    }
}