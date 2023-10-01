package ru.practicum.dto.compilation;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.event.EventShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CompilationDto {
    @NotNull
    private int id;
    @NotNull
    private boolean pinned;
    @NotBlank
    private String title;
    @NotNull
    private Set<EventShortDto> events = new HashSet<>();
}