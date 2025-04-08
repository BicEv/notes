package ru.bicev.notes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.entity.User;
import ru.bicev.notes.exception.DuplicateUserException;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.repository.UserRepository;
import ru.bicev.notes.service.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "test@email.com", "encodedPassword", null);
    }

    @Test
    public void registerUserSuccess() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        
        UserDto savedDto = userService.registerUser("test@email.com", "rawPassword");

        assertNotNull(savedDto);
        assertEquals(user.getEmail(), savedDto.getEmail());
        assertEquals(user.getId(), savedDto.getId());

        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    public void registerUser_DuplicateUser() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        assertThrows(DuplicateUserException.class,
                () -> userService.registerUser("test@email.com", "rawPassword"));
    }

    @Test
    public void checkCredentialsSuccess() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        boolean check = userService.checkCredentials("test@email.com", "rawPassword");

        assertEquals(true, check);

        verify(userRepository, times(1)).findByEmail("test@email.com");
        verify(passwordEncoder, times(1)).matches("rawPassword", "encodedPassword");

    }

    @Test
    public void checkCredentialsFail() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(false);

        boolean check = userService.checkCredentials("test@email.com", "rawPassword");

        assertEquals(false, check);

        verify(userRepository, times(1)).findByEmail("test@email.com");
        verify(passwordEncoder, times(1)).matches("rawPassword", "encodedPassword");

    }

    @Test
    public void checkCredentials_UserNotFoundException() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.checkCredentials("test@email.com", "rawPassword"));
    }

    @Test
    public void getUserByEmailSuccess() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        UserDto foundUser = userService.getUserByEmail("test@email.com");

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());

        verify(userRepository, times(1)).findByEmail("test@email.com");
    }

    @Test
    public void getUserByEmail_UserNotFoundException() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("test@email.com"));

        verify(userRepository, times(1)).findByEmail("test@email.com");
    }

}
