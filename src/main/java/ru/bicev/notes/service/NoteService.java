package ru.bicev.notes.service;

import java.util.List;

import ru.bicev.notes.dto.NoteDto;

public interface NoteService {

    NoteDto createNote(NoteDto noteDto, String email);

    NoteDto editNote(Long noteId, NoteDto noteDto, String email);

    void deleteNote(Long noteId, String email);

    NoteDto addTags(Long noteId, String email, String... tags);

    NoteDto removeTags(Long noteId, String email, String... tags);

    List<NoteDto> findByUser(String email);

    List<NoteDto> findByTagPart(String tagPart, String email);

    NoteDto findByIdAndUser(Long noteId, String email);

    List<NoteDto> findByAllTags(List<String> tags, String email);

    List<String> getAllTags(String email);

}
