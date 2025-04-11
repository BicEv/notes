package ru.bicev.notes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bicev.notes.dto.NoteDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private NoteDto noteDto = new NoteDto(null, null, "Integration note", List.of("First tag", "Second tag"));

    @Test
    public void testRegisterUser() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .param("email", "integration@email.com")
                .param("password", "password"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@email.com"));
    }

    @Test
    public void testLoginUser() throws Exception {
        mockMvc.perform(post("/api/users/login")
                .param("email", "integration@email.com")
                .param("password", "password"))
                .andExpect(status().isOk());

    }

    @Test
    public void testCreateNote() throws Exception {
        mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + obtainJwt("integration@email.com", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(noteDto.getText()));
    }

    private String obtainJwt(String email, String password) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/users/login")
                .param("email", email).param("password", password))
                .andExpect(status().isOk())
                .andReturn();
        String token = result.getResponse().getContentAsString();
        System.out.println("JWT: " + token);
        return token;
    }
}
