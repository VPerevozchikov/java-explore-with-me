package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class NewCompilationDto {
    private Set<Integer> events;
    private boolean pinned;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}