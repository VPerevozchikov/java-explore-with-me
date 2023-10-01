package ru.practicum.dto.compilation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
public class UpdateCompilationRequest {
    private Set<Integer> events;
    private Boolean pinned;
    @Nullable
    @Size(min = 1, max = 50)
    private String title;
}