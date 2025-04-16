package ru.bicev.notes.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import ru.bicev.notes.dto.ErrorResponse;
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

    @Operation(summary = "New note creation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success creation"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.createNote(noteDto, getEmailFromPrincipal());
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieving note by its id and current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note found"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> findByIdAndUser(
            @Parameter(name = "id", description = "Note id", required = true) @PathVariable Long noteId) {
        NoteDto foundNote = noteService.findByIdAndUser(noteId, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNote);
    }

    @Operation(summary = "Note editing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success editing"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteDto> updateNote(
            @Parameter(name = "id", description = "Note id", required = true) @PathVariable Long noteId,
            @Valid @RequestBody NoteDto noteDto) {
        NoteDto editedNote = noteService.editNote(noteId, noteDto, getEmailFromPrincipal());
        return ResponseEntity.ok(editedNote);
    }

    @Operation(summary = "Deleting note by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success creation"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("{noteId}")
    public ResponseEntity<Void> deleteNote(
            @Parameter(name = "id", description = "Note id", required = true) @PathVariable Long noteId) {
        noteService.deleteNote(noteId, getEmailFromPrincipal());
        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "Addint tags to a note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success adding"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{noteId}/tags")
    public ResponseEntity<NoteDto> addTags(
            @Parameter(name = "id", description = "Note id", required = true) @PathVariable Long noteId,
            @RequestBody String... tags) {
        NoteDto updatedNote = noteService.addTags(noteId, getEmailFromPrincipal(), tags);
        return ResponseEntity.ok(updatedNote);
    }

    @Operation(summary = "Deleting tags from a note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success deleting"),
            @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{noteId}/tags")
    public ResponseEntity<NoteDto> deleteTags(
            @Parameter(name = "id", description = "Note id", required = true) @PathVariable Long noteId,
            @RequestBody String... tags) {
        NoteDto updatedNote = noteService.removeTags(noteId, getEmailFromPrincipal(), tags);
        return ResponseEntity.ok(updatedNote);
    }

    @Operation(summary = "Find a note by list of tags")
    @ApiResponse(responseCode = "200")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/tags")
    public ResponseEntity<List<NoteDto>> findByAllTags(
            @Parameter(name = "tags", description = "List of tags", required = true) @RequestParam List<String> tags) {
        List<NoteDto> foundNotes = noteService.findByAllTags(tags, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @Operation(summary = "Find notes by tag part")
    @ApiResponse(responseCode = "200")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/tagpart")
    public ResponseEntity<List<NoteDto>> findByTagPart(
            @Parameter(name = "tagpart", description = "Tag part", required = true) @RequestParam String tagpart) {
        List<NoteDto> foundNotes = noteService.findByTagPart(tagpart, getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @Operation(summary = "Find all notes for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user")
    public ResponseEntity<List<NoteDto>> findByUser() {
        List<NoteDto> foundNotes = noteService.findByUser(getEmailFromPrincipal());
        return ResponseEntity.ok(foundNotes);
    }

    @Operation(summary = "Find all tags for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
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
