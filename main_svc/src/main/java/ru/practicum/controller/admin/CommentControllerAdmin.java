package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.CommentService;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerAdmin {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(name = "commentId") @Positive int commentId) {
        commentService.deleteCommentByAdmin(commentId);
        log.info("Администратором удален комментарий id={}", commentId);
    }
}
