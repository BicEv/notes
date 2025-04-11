package ru.bicev.notes.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bicev.notes.dto.NoteDto;
import ru.bicev.notes.entity.Note;
import ru.bicev.notes.entity.User;
import ru.bicev.notes.exception.NoteNotFoundException;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.repository.NoteRepository;
import ru.bicev.notes.repository.UserRepository;
import ru.bicev.notes.util.NoteMapper;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public NoteDto createNote(NoteDto noteDto, String email) {
        User currentUser = getCurrentUser(email);
        Note note = new Note();
        note.setText(noteDto.getText());
        note.setTags(noteDto.getTags());
        note.setUser(currentUser);
        Note savedNote = noteRepository.save(note);
        logger.info("Note created: id={}, user={}", savedNote.getId(), email);
        return NoteMapper.toDto(savedNote);
    }

    @Transactional
    @Override
    public void deleteNote(Long noteId, String email) {
        Note foundNote = getNoteByIdAndUser(noteId, getCurrentUser(email));
        noteRepository.delete(foundNote);
        logger.info("Note deleted: id={}, user={}", noteId, email);
    }

    @Transactional
    @Override
    public NoteDto editNote(Long noteId, NoteDto noteDto, String email) {
        Note foundNote = getNoteByIdAndUser(noteId, getCurrentUser(email));
        foundNote.setText(noteDto.getText());
        foundNote.setTags(noteDto.getTags());
        Note editedNote = noteRepository.save(foundNote);
        logger.info("Note edited: id={}, user={}", editedNote.getId(), email);
        return NoteMapper.toDto(editedNote);
    }

    @Transactional
    @Override
    public NoteDto addTags(Long noteId, String email, String... tags) {
        Note foundNote = getNoteByIdAndUser(noteId, getCurrentUser(email));
        if (tags == null || tags.length == 0) {
            logger.warn("No tags provided to add/remove for note with id: {}", noteId);
            return NoteMapper.toDto(foundNote);
        }
        String[] upperTags = Arrays.stream(tags).map(String::toUpperCase).toArray(String[]::new);
        foundNote.addTags(upperTags);
        Note savedNote = noteRepository.save(foundNote);
        logger.info("Added {} tags: {} to note with id: {}", tags.length, Arrays.toString(tags), noteId);
        return NoteMapper.toDto(savedNote);
    }

    @Transactional
    @Override
    public NoteDto removeTags(Long noteId, String email, String... tags) {
        Note foundNote = getNoteByIdAndUser(noteId, getCurrentUser(email));
        if (tags == null || tags.length == 0) {
            logger.warn("No tags provided to add/remove for note with id: {}", noteId);
            return NoteMapper.toDto(foundNote);
        }
        String[] upperTags = Arrays.stream(tags).map(String::toUpperCase).toArray(String[]::new);
        foundNote.removeTags(upperTags);
        Note savedNote = noteRepository.save(foundNote);
        logger.info("Removed {} tags: {} to note with id: {}", tags.length, Arrays.toString(tags), noteId);
        return NoteMapper.toDto(savedNote);

    }

    @Transactional(readOnly = true)
    @Override
    public List<NoteDto> findByAllTags(List<String> tags, String email) {
        logger.info("Searched notes with tags: {} for user={}", tags, email);
        List<String> upperTags = tags.stream().map(String::toUpperCase).collect(Collectors.toList());
        return findNotes(user -> noteRepository.findByAllTags(upperTags, upperTags.size(), user), email);
    }

    @Transactional(readOnly = true)
    @Override
    public NoteDto findByIdAndUser(Long noteId, String email) {
        Note foundNote = getNoteByIdAndUser(noteId, getCurrentUser(email));
        logger.info("Searched note with id: {} and user={}", noteId, email);
        return NoteMapper.toDto(foundNote);
    }

    @Transactional(readOnly = true)
    @Override
    public List<NoteDto> findByTagPart(String tagPart, String email) {
        logger.info("Searched notes with tagPart: {} for user={}", tagPart, email);
        return findNotes(user -> noteRepository.findByTagPart(tagPart.toUpperCase(), user), email);
    }

    @Transactional(readOnly = true)
    @Override
    public List<NoteDto> findByUser(String email) {
        logger.info("Searched notes for user={}", email);
        return findNotes(noteRepository::findByUser, email);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> getAllTags(String email) {
        User currentUser = getCurrentUser(email);
        List<Note> notes = noteRepository.findByUser(currentUser);
        if (notes.isEmpty()) {
            logger.info("No notes found for user={}", email);
            return Collections.emptyList();
        }
        logger.info("Searched tags for user={}", email);
        return notes.stream()
                .flatMap(note -> note.getTags().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private Note getNoteByIdAndUser(Long noteId, User user) {
        return noteRepository.findByIdAndUser(noteId, user).orElseThrow(() -> {
            logger.warn("Note with id: {} was not found", noteId);
            return new NoteNotFoundException("Note was not found");
        });
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("User with email: {} was not found", email);
            return new UserNotFoundException("User not found");
        });
    }

    private List<NoteDto> findNotes(Function<User, List<Note>> findFunction, String email) {
        User currentUser = getCurrentUser(email);
        return NoteMapper.toDtoList(findFunction.apply(currentUser));
    }

}
