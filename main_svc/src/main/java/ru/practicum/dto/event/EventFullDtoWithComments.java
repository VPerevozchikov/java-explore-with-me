package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.comment.CommentDto;

import java.util.List;

@Getter
@Setter
public class EventFullDtoWithComments extends EventFullDto {
    private List<CommentDto> comments;
}
