package ru.practicum.dto.category;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CategoryDto {

    private int id;
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}