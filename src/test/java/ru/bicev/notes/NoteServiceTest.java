package ru.bicev.notes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.bicev.notes.dto.NoteDto;
import ru.bicev.notes.entity.Note;
import ru.bicev.notes.entity.User;
import ru.bicev.notes.exception.NoteNotFoundException;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.repository.NoteRepository;
import ru.bicev.notes.repository.UserRepository;
import ru.bicev.notes.service.NoteServiceImpl;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User firstU;

    private Note firstN;
    private Note secondN;
    private Note updatedNote;

    private List<Note> notes;

    private NoteDto noteDto;

    @BeforeEach
    public void setUp() {
        firstU = new User(1L, "first@email.com", "rawPassword", null);

        firstN = new Note(firstU, "First note");
        secondN = new Note(firstU, "Second note");
        updatedNote = new Note(firstU, "Updated note");
        updatedNote.setTags(List.of("TAG1", "TAG2"));

        noteDto = new NoteDto(null, "first@email.com", "First note", List.of("TAG1", "TAG2", "TAG3"));

        notes = List.of(
                new Note(firstU, "List note 1"),
                new Note(firstU, "List note 2"));
        notes.get(0).addTags("TAG1");
        notes.get(1).addTags("TAG2", "TAG3");
    }

    //region createNote()
    @Test
    public void createNoteSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.save(any(Note.class))).thenReturn(firstN);

        NoteDto createdNote = noteService.createNote(noteDto, "first@email.com");

        assertNotNull(createdNote);
        assertEquals(firstN.getId(), createdNote.getId());
        assertEquals(firstN.getText(), createdNote.getText());

        verify(noteRepository, times(1)).save(any(Note.class));
        verify(userRepository, times(1)).findByEmail("first@email.com");
    }

    @Test
    public void createNote_UserNotFound() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.createNote(noteDto, "first@email.com"));
    }
    //endregion

    //region deleteNote()
    @Test
    public void deleteNoteSuccess() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.of(firstN));
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));

        noteService.deleteNote(1L, "first@email.com");

        verify(noteRepository, times(1)).findByIdAndUser(eq(1L), any(User.class));
        verify(noteRepository, times(1)).delete(any(Note.class));
    }

    @Test
    public void deleteNote_NoteNotFoundException() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.empty());
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));

        assertThrows(NoteNotFoundException.class, () -> noteService.deleteNote(1L, "first@email.com"));
    }

    @Test
    public void deleteNote_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.deleteNote(1L, "first@email.com"));
    }
    //endregion

    //region updateNote()
    @Test
    public void updateNoteSuccess() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.of(firstN));
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        NoteDto updatedNoteDto = noteService.editNote(1L, noteDto, "first@email.com");

        assertNotNull(updatedNoteDto);
        assertEquals(updatedNote.getId(), updatedNoteDto.getId());
        assertEquals(updatedNote.getText(), updatedNoteDto.getText());

        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    public void updateNote_NoteNotFoundException() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.empty());
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));

        assertThrows(NoteNotFoundException.class, () -> noteService.editNote(1L, noteDto, "first@email.com"));
    }

    @Test
    public void updateNote_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.editNote(1L, noteDto, "first@email.com"));
    }
    //endregion

    //region addTags()
    @Test
    public void addTagsSuccess() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.of(firstN));
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        NoteDto taggedNote = noteService.addTags(1L, "first@email.com", "TAG1", "TAG2");

        assertNotNull(taggedNote);
        assertEquals(updatedNote.getTags(), taggedNote.getTags());

        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    public void addTags_NoteNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.addTags(1L, "first@email.com", "TAG1", "TAG2"));
    }

    @Test
    public void addTags_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.addTags(1L, "first@email.com", "TAG1", "TAG2"));
    }
    //endregion

    //region removeTags()
    @Test
    public void removeTagsSuccess() {
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.of(secondN));
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.save(any(Note.class))).thenReturn(secondN);

        NoteDto taggedNote = noteService.removeTags(1L, "first@email.com", "TAG1", "TAG2");

        assertNotNull(taggedNote);
        assertEquals(secondN.getTags(), taggedNote.getTags());

        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    public void removeTags_NoteNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByIdAndUser(eq(1L), any(User.class))).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.removeTags(1L, "first@email.com", "TAG1", "TAG2"));
    }

    @Test
    public void removeTags_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.removeTags(1L, "first@email.com", "TAG1", "TAG2"));
    }
    //endregion

    //region findByAllTags()
    @Test
    public void findByAllTagsSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByAllTags(anyList(), anyLong(), any(User.class))).thenReturn(notes);

        List<NoteDto> foundNotes = noteService.findByAllTags(List.of("TAG1", "TAG2"), "first@email.com");
        assertNotNull(foundNotes);
        assertEquals(notes.size(), foundNotes.size());
        assertEquals(notes.get(0).getText(), foundNotes.get(0).getText());
        assertEquals(notes.get(1).getText(), foundNotes.get(1).getText());

        verify(noteRepository, times(1)).findByAllTags(anyList(), anyLong(), any(User.class));
    }

    @Test
    public void findByAllTags_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> noteService.findByAllTags(List.of("TAG1", "TAG2"), "first@email.com"));
    }
    //endregion

    //region findByIdAndUser()
    @Test
    public void findByIdAndUserSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByIdAndUser(1L, firstU)).thenReturn(Optional.of(firstN));

        NoteDto foundNote = noteService.findByIdAndUser(1L, "first@email.com");

        assertNotNull(foundNote);
        assertEquals(firstN.getText(), foundNote.getText());

        verify(noteRepository, times(1)).findByIdAndUser(1L, firstU);
    }

    @Test
    public void findByIdAndUser_NoteNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByIdAndUser(1L, firstU)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.findByIdAndUser(1L, "first@email.com"));
    }

    @Test
    public void findByIdAndUser_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.findByIdAndUser(1L, "first@email.com"));
    }
    //endregion

    //region findByTagPart()
    @Test
    public void findByTagPartSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByTagPart("AG", firstU)).thenReturn(notes);

        List<NoteDto> foundNotes = noteService.findByTagPart("AG", "first@email.com");
        assertNotNull(foundNotes);
        assertEquals(notes.size(), foundNotes.size());
        assertEquals(notes.get(0).getText(), foundNotes.get(0).getText());
        assertEquals(notes.get(1).getText(), foundNotes.get(1).getText());

        verify(noteRepository, times(1)).findByTagPart("AG", firstU);
    }

    @Test
    public void findByTagPartUserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.findByTagPart("AG", "first@email.com"));
    }
    //endregion

    //region findByUser()
    @Test
    public void findByUserSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByUser(firstU)).thenReturn(notes);

        List<NoteDto> foundNotes = noteService.findByUser("first@email.com");

        assertNotNull(foundNotes);
        assertEquals(notes.size(), foundNotes.size());
        assertEquals(notes.get(0).getText(), foundNotes.get(0).getText());
        assertEquals(notes.get(1).getText(), foundNotes.get(1).getText());

        verify(noteRepository, times(1)).findByUser(firstU);
    }

    @Test
    public void findByUser_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.findByUser("first@email.com"));
    }
    //endregion

    //region getAllTags()
    @Test
    public void getAllTagsSuccess() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.of(firstU));
        when(noteRepository.findByUser(firstU)).thenReturn(notes);

        List<String> tags = noteService.getAllTags("first@email.com");
        assertNotNull(tags);
        assertEquals(List.of("TAG1", "TAG2", "TAG3").size(), tags.size());
        assertEquals(List.of("TAG1", "TAG2", "TAG3"), tags);

        verify(noteRepository, times(1)).findByUser(firstU);
    }

    @Test
    public void getAllTags_UserNotFoundException() {
        when(userRepository.findByEmail("first@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> noteService.getAllTags("first@email.com"));
    }
    //endregion

}
