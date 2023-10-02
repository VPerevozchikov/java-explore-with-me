package ru.practicum.dto.participationRequest;

import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParticipationMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto toDto(ParticipationRequest partRequest) {
        ParticipationRequestDto dto = new ParticipationRequestDto();

        dto.setId(partRequest.getId());
        dto.setCreated(partRequest.getCreated().format(TIME_FORMAT));
        dto.setEvent(partRequest.getEvent().getId());
        dto.setRequester(partRequest.getRequester().getId());
        dto.setStatus(partRequest.getStatus().toString());

        return dto;
    }

    public static ParticipationRequest toPr(ParticipationRequestDto dto, Event event, User user) {
        ParticipationRequest pr = new ParticipationRequest();

        pr.setId(dto.getId());
        pr.setCreated(LocalDateTime.parse(dto.getCreated(), TIME_FORMAT));
        pr.setEvent(event);
        pr.setRequester(user);
        pr.setStatus(RequestStatus.valueOf(dto.getStatus()));

        return pr;
    }
}
