package ru.bicev.notes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import ru.bicev.notes.dto.JwtResponse;
import ru.bicev.notes.dto.LoginRequest;
import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.exception.AccessDeniedException;
import ru.bicev.notes.service.JwtService;
import ru.bicev.notes.service.UserService;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService, AuthenticationManager authManager, JwtService jwtService) {
        this.userService = userService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserDto userDto = userService.registerUser(loginRequest.getEmail(), loginRequest.getPassword());
        logger.info("Registering user with email: {}", loginRequest.getEmail());
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        if (!userService.checkCredentials(loginRequest.getEmail(), loginRequest.getPassword())) {
            throw new AccessDeniedException("Invalid password");
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        String token = jwtService.generateToken(loginRequest.getEmail());
        JwtResponse jwt = new JwtResponse(token);
        logger.info("User with email: {} logged in", loginRequest.getEmail());
        return ResponseEntity.ok(jwt);
    }

}
