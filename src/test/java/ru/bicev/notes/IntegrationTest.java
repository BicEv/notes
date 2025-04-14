package ru.bicev.notes;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bicev.notes.dto.NoteDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long savedNoteId;
    String[] tagsArray = { "tag1", "tag2" };
    private NoteDto noteDto = new NoteDto(null, null, "Integration note", List.of("First tag", "Second tag"));
    private NoteDto updateNoteDto = new NoteDto(null, null, "Updated note", List.of("First tag"));

    @Test
    @Order(1)
    public void testRegisterUser() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .param("email", "integrationTest@email.com")
                .param("password", "password"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integrationTest@email.com"));
    }

    @Order(2)
    @Test
    public void testLoginUser() throws Exception {
        mockMvc.perform(post("/api/users/login")
                .param("email", "integrationTest@email.com")
                .param("password", "password"))
                .andExpect(status().isOk());

    }

    @Test
    @Order(3)
    public void testCreateNote() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/notes")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(noteDto.getText()))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        NoteDto createdNote = objectMapper.readValue(json, NoteDto.class);
        savedNoteId = createdNote.getId();
    }

    @Test
    @Order(4)
    public void testGetNoteById() throws Exception {
        mockMvc.perform(get("/api/notes/" + savedNoteId)
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(noteDto.getText()));
    }

    @Test
    @Order(11)
    public void testUpdateNote() throws Exception {
        mockMvc.perform(put("/api/notes/" + savedNoteId)
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(updateNoteDto.getText()));
    }

    @Test
    @Order(12)
    public void testDeleteNote() throws Exception {
        mockMvc.perform(delete("/api/notes/" + savedNoteId)
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password")))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(5)
    public void testAddTags() throws Exception {
        mockMvc.perform(post("/api/notes/" + savedNoteId + "/tags")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsArray)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(noteDto.getText()))
                .andExpect(jsonPath("$.tags.size()").value(4));
    }

    @Test
    @Order(6)
    public void testDeleteTags() throws Exception {
        mockMvc.perform(delete("/api/notes/" + savedNoteId + "/tags")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsArray)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags.size()").value(2));
    }

    @Test
    @Order(7)
    public void testFindByAllTags() throws Exception {
        mockMvc.perform(get("/api/notes/tags")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .param("tags", "First tag", "Second tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(8)
    public void testFindByTagPart() throws Exception {
        mockMvc.perform(get("/api/notes/tagpart")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
                .param("tagpart", "ta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(9)
    public void testFindByUser() throws Exception {
        mockMvc.perform(get("/api/notes/user")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(10)
    public void testGetAllTags() throws Exception {
        mockMvc.perform(get("/api/notes/tags/all")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @Order(13)
    public void testGetNonExistentNote_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/notes/99999")
                .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    public void testRegisterUser_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .param("email", "integrationTest@email.com")
                .param("password", "password"))
                .andExpect(status().isConflict());
    }

    @Order(15)
    @Test
    public void testLoginUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/users/login")
                .param("email", "integrationTest@email.com")
                .param("password", "NotMatchingPass"))
                .andExpect(status().isForbidden());

    }

    @Order(16)
    @Test
    public void testLoginUser_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/users/login")
                .param("email", "notvalid@email.com")
                .param("password", "NotMatchingPass"))
                .andExpect(status().isNotFound());

    }

    @Test
    @Order(17)
    public void testEmptyJwt_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/notes/99999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    public void testInvalidJwt_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/notes/99999")
                .header("Authorization", "Bearer notatoken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(19)
    public void testInvalidNote_shouldReturn500() throws Exception {
        mockMvc.perform(post("/api/notes")
        .header("Authorization", "Bearer " + obtainJwt("integrationTest@email.com", "password"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString("nothing")))
        .andExpect(status().isInternalServerError());
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
