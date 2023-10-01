package ru.practicum.dto.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserShortDto {
    @NotNull
    private int id;
    @NotBlank
    private String name;
}