package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentMapper;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentJpaRepository;
import ru.practicum.repository.EventJpaRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentJpaRepository commentJpaRepository;
    private final UserService userService;
    private final EventJpaRepository eventJpaRepository;

    @Transactional
    public CommentDto createComment(int userId, CommentDto commentDto) {
        User commentAuthor = UserMapper.toUser(userService.getUserById(userId));
        int eventId = commentDto.getEventId();
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + eventId + " не найдено"));
        Comment comment = CommentMapper.toComment(commentDto, commentAuthor, event);
        Comment savedComment = commentJpaRepository.save(comment);

        return CommentMapper.toDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(int userId, int commentId, CommentDto updatedComment) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден"));

        if (comment.getAuthor().getId() != userId) {
            throw new BadParameterException("Пользователь с id=" + userId + "  не является автором комментария");
        }
        comment.setText(updatedComment.getText());
        comment.setUpdated(LocalDateTime.now());
        Comment savedComment = commentJpaRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Transactional
    public void deleteComment(int userId, int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден"));

        if (comment.getAuthor().getId() != userId) {
            throw new BadParameterException("Пользователь с id=" + userId + "  не является автором комментария");
        }
        commentJpaRepository.deleteById(commentId);
    }

    @Transactional
    public void deleteCommentByAdmin(int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден"));

        commentJpaRepository.deleteById(commentId);
    }

    public CommentDto getComment(int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с id=" + commentId + " не найден"));
        return CommentMapper.toDto(comment);
    }
}
