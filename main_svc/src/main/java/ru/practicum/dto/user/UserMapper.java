package ru.practicum.dto.user;

import ru.practicum.model.User;

public class UserMapper {

    public static UserShortDto toShortDto(User user) {
        UserShortDto shortDto = new UserShortDto();
        shortDto.setId(user.getId());
        shortDto.setName(user.getName());
        return shortDto;
    }

    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public static User toUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());
        return user;
    }

    public static User toUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        return user;
    }
}