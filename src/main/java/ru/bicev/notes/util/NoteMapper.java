package ru.bicev.notes.util;

import java.util.ArrayList;
import java.util.List;

import ru.bicev.notes.dto.NoteDto;
import ru.bicev.notes.entity.Note;
import ru.bicev.notes.entity.User;

public class NoteMapper {

    public static Note toEntity(NoteDto noteDto, User user) {
        Note note = new Note();
        if (noteDto.getId() != null) {
            note.setId(noteDto.getId());
        }
        note.setText(noteDto.getText());
        note.setUser(user);
        if (noteDto.getTags() != null && !noteDto.getTags().isEmpty()) {
            note.setTags(noteDto.getTags());
        }

        return note;
    }

    public static NoteDto toDto(Note note) {
        NoteDto noteDto = new NoteDto();
        noteDto.setId(note.getId());
        noteDto.setUserEmail(note.getUser().getEmail());
        noteDto.setText(note.getText());
        noteDto.setTags(note.getTags());
        return noteDto;
    }

    public static List<Note> toEntityList(List<NoteDto> noteDtos, User user) {
        List<Note> notes = new ArrayList<>();
        for (NoteDto noteDto : noteDtos) {
            notes.add(toEntity(noteDto, user));
        }
        return notes;
    }

    public static List<NoteDto> toDtoList(List<Note> notes) {
        List<NoteDto> noteDtos = new ArrayList<>();
        for (Note note : notes) {
            noteDtos.add(toDto(note));
        }
        return noteDtos;
    }

}
