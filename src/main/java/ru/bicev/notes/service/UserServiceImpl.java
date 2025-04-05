package ru.bicev.notes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.entity.User;
import ru.bicev.notes.exception.DuplicateUserException;
import ru.bicev.notes.exception.UserNotFoundException;
import ru.bicev.notes.util.UserMapper;
import ru.bicev.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto registerUser(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Trying to register user with duplicate email: {}", email);
            throw new DuplicateUserException("Email already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        logger.info("Saving user with email: {}", email);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public boolean checkCredentials(String email, String password) {
        User foundUser = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("User with email: {} was not found", email);
            return new UserNotFoundException("User not found");
        });
        logger.info("Checked user with email: {}", email);
        return passwordEncoder.matches(password, foundUser.getPassword());

    }

    @Override
    public UserDto getUserByEmail(String email) {
        User foundUser = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.warn("User with email: {} was not found", email);
            return new UserNotFoundException("User not found");
        });
        logger.info("Searched for user with email: {}", email);
        return UserMapper.toDto(foundUser);
    }

}
