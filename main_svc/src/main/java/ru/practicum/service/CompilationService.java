package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationMapper;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CompilationJpaRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompilationService {
    private final CompilationJpaRepository compilationJpaRepository;
    private final EventService eventService;
    private final UserService userService;

    @Autowired
    public CompilationService(CompilationJpaRepository compilationJpaRepository,
                              EventService eventService, UserService userService) {
        this.compilationJpaRepository = compilationJpaRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    public List<CompilationDto> getAllComps(boolean pinned, int from, int size) {
        if (from < 0 || size < 1) {
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Compilation> compilations = compilationJpaRepository.findByPinned(pinned, page);
        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompById(int compId) {
        if (compId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }
        Optional<Compilation> compilationOptional = compilationJpaRepository.findById(compId);
        if (compilationOptional.isEmpty()) {
            throw new ElementNotFoundException("Элемент с id=" + compId + " не найден");
        }
        return CompilationMapper.toDto(compilationOptional.get());
    }

    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Set<Integer> newCompilationEventIds = newCompilationDto.getEvents();
        Set<Event> eventSet;
        if (newCompilationEventIds != null && !newCompilationEventIds.isEmpty()) {
            Set<EventFullDto> eventDtoSet = eventService.getEventsByIdSet(newCompilationDto.getEvents());
            List<Integer> userIds = eventDtoSet.stream()
                    .map(EventFullDto::getInitiator)
                    .map(UserShortDto::getId)
                    .collect(Collectors.toList());
            List<UserDto> usersDto = userService.getAllUsers(userIds); //получили список UserDto
            List<User> users = usersDto.stream()
                    .map(UserMapper::toUser)
                    .collect(Collectors.toList()); //преоразовали в список User

            Map<Integer, User> eventInitiatorMap = eventDtoSet.stream()
                    .collect(Collectors.toMap(
                            EventFullDto::getId,
                            e -> {
                                return users.stream()
                                        .filter(u -> u.getId() == e.getInitiator().getId())
                                        .findFirst()
                                        .get();
                            }
                    ));

            eventSet = eventDtoSet.stream()
                    .map(e -> EventMapper.toEvent(e, eventInitiatorMap.get(e.getId())))
                    .collect(Collectors.toSet());
        } else {
            eventSet = new HashSet<Event>();
        }
        Compilation compilation = compilationJpaRepository.save(CompilationMapper.toComp(newCompilationDto, eventSet)); //сохраняем в ремозитории
        return CompilationMapper.toDto(compilation);
    }

    public void deleteById(int compId) {
        Optional<Compilation> compilationOptional = compilationJpaRepository.findById(compId);
        if (compilationOptional.isEmpty()) {
            throw new ElementNotFoundException("Подборка с id= " + compId + " не найден");
        }
        compilationJpaRepository.deleteById(compId);
    }

    public CompilationDto update(int compId, UpdateCompilationRequest updateRequest) {

        Optional<Compilation> compilationOptional = compilationJpaRepository.findById(compId);
        if (compilationOptional.isEmpty()) {
            throw new ElementNotFoundException("Подборка с id=" + compId + " не найдена");
        }
        Compilation compilation = compilationOptional.get();

        Set<Integer> eventIds = updateRequest.getEvents();
        Set<Event> eventsSet;
        if (eventIds == null || eventIds.isEmpty()) {
            eventsSet = new HashSet<>();
        } else {
            eventsSet = eventService.getEventsByIds(eventIds);
        }

        Boolean pinned = updateRequest.getPinned();
        if (pinned != null) {
            compilation.setPinned(pinned);
        }
        String title = updateRequest.getTitle();
        if (title != null) {
            compilation.setTitle(title);
        }
        compilation.setEvents(eventsSet);
        compilation = compilationJpaRepository.save(compilation);

        return CompilationMapper.toDto(compilation);
    }
}