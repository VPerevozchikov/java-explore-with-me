package ru.practicum.dto.compilation;

import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();

        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.isPinned());
        compilationDto.setTitle(compilation.getTitle());

        Set<Event> events = compilation.getEvents();
        if (events == null || events.size() == 0) {
            return compilationDto;
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList()));
        compilationDto.setEvents(compilation.getEvents().stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toSet()));

        return compilationDto;
    }

    public Compilation toComp(CompilationDto compilationDto) {
        Compilation compilation = new Compilation();

        compilation.setId(compilationDto.getId());
        compilation.setTitle(compilationDto.getTitle());
        compilation.setPinned(compilationDto.isPinned());

        return compilation;
    }

    public static Compilation toComp(NewCompilationDto newCompilationDto, Set<Event> events) {
        Compilation compilation = new Compilation();

        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(events);
        compilation.setPinned(newCompilationDto.isPinned());

        return compilation;
    }
}