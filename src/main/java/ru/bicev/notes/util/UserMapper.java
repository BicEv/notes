package ru.bicev.notes.util;

import ru.bicev.notes.dto.UserDto;
import ru.bicev.notes.entity.User;

public class UserMapper {

    public static User toEntity(UserDto userDto) {
        User user = new User();
        if (userDto.getId() != null) {
            user.setId(userDto.getId());
        }
        user.setEmail(userDto.getEmail());
        return user;
    }

    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

}
