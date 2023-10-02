package ru.practicum.dto.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class UserDto {
    private int id;
    @Email
    @Pattern(regexp = ".+@.+\\..+")
    @NotNull
    @Size(min = 6, max = 254)
    private String email;
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}