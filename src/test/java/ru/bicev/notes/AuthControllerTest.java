package ru.bicev.notes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.bicev.notes.config.TestSecurityConfig;
import ru.bicev.notes.controller.AuthController;
import ru.bicev.notes.controller.GlobalExceptionHandler;
import ru.bicev.notes.dto.LoginRequest;
import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.exception.DuplicateUserException;
import ru.bicev.notes.service.JwtService;
import ru.bicev.notes.service.UserService;

@WebMvcTest(AuthController.class)
@Import({ TestSecurityConfig.class, GlobalExceptionHandler.class })
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    LoginRequest loginRequest = new LoginRequest("test@email.com", "password");
    String token = "mocked-jwt-token";
    private UserDto userDto = new UserDto(1L, "test@email.com");
    String expectedJson = """
            {
                "token": "mocked-jwt-token"
            }
            """;

    @Test
    public void registerUserSuccess() throws Exception {
        when(userService.registerUser(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(userDto);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(loginRequest.getEmail()))
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    void testLogin_returnsJwtToken() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));

        when(jwtService.generateToken(loginRequest.getEmail())).thenReturn(token);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void duplicateUserExceptionTest() throws Exception {
        when(userService.registerUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenThrow(new DuplicateUserException("Email already in use"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isConflict());

    }

    @Test
    public void genericExceptionTest() throws Exception {
        when(userService.registerUser(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenThrow(new RuntimeException("Exception"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

    }

}
