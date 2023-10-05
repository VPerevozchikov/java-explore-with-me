package ru.practicum.dto.comment;

import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

public class CommentMapper {

    public static Comment toComment(CommentDto commentDto, User author, Event event) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(commentDto.getCreated());
        comment.setUpdated(commentDto.getUpdated());
        comment.setId(commentDto.getId());

        return comment;
    }

    public static CommentDto toDto(Comment comment) {
        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setCreated(comment.getCreated());
        commentDto.setUpdated(comment.getUpdated());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setEventId(comment.getEvent().getId());
        return commentDto;
    }
}
