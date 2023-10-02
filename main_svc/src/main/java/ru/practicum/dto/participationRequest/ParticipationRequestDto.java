package ru.practicum.dto.participationRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipationRequestDto {
    private int id;
    private String created;
    private int event;
    private int requester;
    private String status;
}
