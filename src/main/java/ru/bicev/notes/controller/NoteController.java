package ru.bicev.notes.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ru.bicev.notes.dto.NoteDto;
import ru.bicev.notes.service.NoteService;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.createNote(noteDto, getEmailFromPrincipal());
        return ResponseEntity.ok(createdNote);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> findByIdAndUser(@PathVariable Long noteId) {
        NoteDto foundNote = noteService.findByIdAndUser(noteId, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNote);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long noteId, @Valid @RequestBody NoteDto noteDto) {
        NoteDto editedNote = noteService.editNote(noteId, noteDto, getEmailFromPrincipal());
        return ResponseEntity.ok(editedNote);
    }

    @DeleteMapping("{noteId}")
    public ResponseEntity deleteNote(@PathVariable Long noteId) {
        noteService.deleteNote(noteId, getEmailFromPrincipal());
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{noteId}/tags")
    public ResponseEntity<NoteDto> addTags(@PathVariable Long noteId, @RequestBody String... tags) {
        NoteDto updatedNote = noteService.addTags(noteId, getEmailFromPrincipal(), tags);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{noteId}/tags")
    public ResponseEntity<NoteDto> deleteTags(@PathVariable Long noteId, @RequestBody String... tags) {
        NoteDto updatedNote = noteService.removeTags(noteId, getEmailFromPrincipal(), tags);
        return ResponseEntity.ok(updatedNote);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<NoteDto>> findByAllTags(@RequestParam List<String> tags) {
        List<NoteDto> foundNotes = noteService.findByAllTags(tags, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @GetMapping("/tagpart")
    public ResponseEntity<List<NoteDto>> findByTagPart(@RequestParam String tagpart) {
        List<NoteDto> foundNotes = noteService.findByTagPart(tagpart, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @GetMapping("/user")
    public ResponseEntity<List<NoteDto>> findByUser() {
        List<NoteDto> foundNotes = noteService.findByUser(getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @GetMapping("/tags/all")
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = noteService.getAllTags(getEmailFromPrincipal());
        return ResponseEntity.ok(tags);
    }

    private String getEmailFromPrincipal() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Getting email from principal: {}", email);
        return email;
    }

}
