package ru.bicev.notes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import ru.bicev.notes.config.TestSecurityConfig;
import ru.bicev.notes.controller.GlobalExceptionHandler;
import ru.bicev.notes.controller.NoteController;
import ru.bicev.notes.dto.NoteDto;
import ru.bicev.notes.exception.AccessDeniedException;
import ru.bicev.notes.exception.NoteNotFoundException;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.service.NoteService;

@WebMvcTest(NoteController.class)
@Import({ TestSecurityConfig.class, GlobalExceptionHandler.class })
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    List<String> tags = List.of("tag1", "tag2");
    private NoteDto noteDto = new NoteDto(1L, "test@email.com", "Test note", tags);
    private NoteDto savedNote = new NoteDto(1L, "test@email.com", "Test note", tags);
    String[] tagsArray = { "tag1", "tag2" };

    @Test
    @WithMockUser(username = "test@email.com")
    public void createNoteSuccess() throws Exception {
        when(noteService.createNote(any(), anyString())).thenReturn(savedNote);

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.text").value("Test note"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void findByIdAndUserSuccess() throws Exception {
        when(noteService.findByIdAndUser(eq(1L), anyString())).thenReturn(savedNote);

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test note"))
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void updateNoteSuccess() throws Exception {
        when(noteService.editNote(eq(1L), any(NoteDto.class), anyString())).thenReturn(savedNote);

        mockMvc.perform(put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test note"))
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void deleteNoteSuccess() throws Exception {
        doNothing().when(noteService).deleteNote(eq(1L), anyString());

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void addTagsSuccess() throws Exception {
        when(noteService.addTags(eq(1L), anyString(), eq(tagsArray))).thenReturn(savedNote);

        mockMvc.perform(post("/api/notes/1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsArray)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test note"))
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void deleteTagsSuccess() throws Exception {
        when(noteService.removeTags(eq(1L), anyString(), eq(tagsArray))).thenReturn(savedNote);

        mockMvc.perform(delete("/api/notes/1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsArray)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test note"))
                .andExpect(jsonPath("$.id").value(1));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void findByAllTagsSuccess() throws Exception {
        when(noteService.findByAllTags(eq(tags), anyString())).thenReturn(List.of(noteDto, savedNote));

        mockMvc.perform(get("/api/notes/tags")
                .param("tags", "tag1", "tag2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void findByTagPartSuccess() throws Exception {
        when(noteService.findByTagPart(eq("ag"), anyString())).thenReturn(List.of(noteDto, savedNote));

        mockMvc.perform(get("/api/notes/tagpart")
                .param("tagpart", "ag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void findByUserSuccess() throws Exception {
        when(noteService.findByUser(anyString())).thenReturn(List.of(noteDto, savedNote));

        mockMvc.perform(get("/api/notes/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void findAllTagsSuccess() throws Exception {
        when(noteService.getAllTags(anyString())).thenReturn(tags);

        mockMvc.perform(get("/api/notes/tags/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void userNotFoundTest() throws Exception {
        when(noteService.createNote(any(), anyString())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void noteNotFoundTest() throws Exception {
        when(noteService.findByIdAndUser(eq(1L), anyString())).thenThrow(new NoteNotFoundException("Note not found"));

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void accessDemiedExceptionTest() throws Exception {
        when(noteService.findByIdAndUser(eq(1L), anyString())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test@email.com")
    public void genericExceptionTest() throws Exception {
        when(noteService.findByIdAndUser(eq(1L), anyString())).thenThrow(new RuntimeException("Exception"));

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isInternalServerError());

    }

}
