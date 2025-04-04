package ru.bicev.notes.service;

import ru.bicev.notes.dto.UserDto;

public interface UserService {

    UserDto registerUser(String email, String password);

    boolean checkCredentials(String email, String password);

    UserDto getUserByEmail(String email);

}
