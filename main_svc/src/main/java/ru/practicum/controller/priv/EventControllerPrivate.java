package ru.practicum.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
public class EventControllerPrivate {
    private final EventService eventService;

    @Autowired
    public EventControllerPrivate(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto postEvent(@PathVariable(name = "userId") int userId,
                                  @Valid @RequestBody NewEventDto newEventDto) {
        EventFullDto eventDto = eventService.createEvent(newEventDto, userId);
        log.info("Создано новое событие title={}, date={}", eventDto.getTitle(), eventDto.getEventDate());
        return eventDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByUser(@PathVariable(name = "userId") int userId,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        List<EventShortDto> eventShortDtos = eventService.getAllByUser(userId, from, size);
        log.info("Получен список событий, добавленных пользователем с id={}", userId);
        return eventShortDtos;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDtoWithComments getEventByUserAndId(@PathVariable(name = "userId") int userId,
                                                        @PathVariable(name = "eventId") int eventId) {
        EventFullDtoWithComments eventFullDto = eventService.getByUserAndId(userId, eventId);
        log.info("Получено событие с Id={} , добавленное пользователем с id={}", eventId, userId);
        return eventFullDto;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto patchEvent(@PathVariable(name = "userId") int userId,
                                   @PathVariable(name = "eventId") int eventId,
                                   @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        EventFullDto eventFullDto = eventService.patchEvent(userId, eventId, updateRequest);
        log.info("Обновлено событие с Id={} , добавленное пользователем с id={}", eventId, userId);
        return eventFullDto;
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getParticipationInfo(@PathVariable(name = "userId") int userId,
                                                              @PathVariable(name = "eventId") int eventId) {
        List<ParticipationRequestDto> partRequestDtoList = eventService.getParticipationInfo(userId, eventId);
        log.info("Получена информация о запросах на учатсие в событии с Id={} , добавленное пользователем с id={}",
                eventId, userId);
        return partRequestDtoList;
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult patchEventStatus(@PathVariable(name = "userId") int userId,
                                                           @PathVariable(name = "eventId") int eventId,
                                                           @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        EventRequestStatusUpdateResult updateStatusResult = eventService.updateStatus(userId, eventId, statusUpdateRequest);
        log.info("Обновлен статус события с Id={} , добавленное пользователем с id={}. Статус = {}",
                eventId, userId, statusUpdateRequest.getStatus().toString());
        return updateStatusResult;
    }
}