package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postComment(@PathVariable(name = "userId") @Positive int userId,
                                  @Valid @RequestBody CommentDto newComment) {
        CommentDto commentDto = commentService.createComment(userId, newComment);
        log.info("Создан комментарий id={} от пользователя userId={} к событию eventId={}", commentDto.getId(), userId, commentDto.getEventId());
        return commentDto;
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable(name = "userId") @Positive int userId,
                                    @PathVariable(name = "commentId") @Positive int commentId,
                                    @Valid @RequestBody CommentDto updatedComment) {
        CommentDto commentDto = commentService.updateComment(userId, commentId, updatedComment);
        log.info("Обновлен комментарий id={} от пользователя userId={} к событию eventId={}", commentDto.getId(), userId, commentDto.getEventId());
        return commentDto;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(name = "userId") @Positive int userId,
                              @PathVariable(name = "commentId") @Positive int commentId) {
        commentService.deleteComment(userId, commentId);
        log.info("Удален комментарий id={} от пользователя userId={}", commentId, userId);
    }
}
