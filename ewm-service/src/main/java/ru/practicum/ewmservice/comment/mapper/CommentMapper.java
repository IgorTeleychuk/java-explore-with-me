package ru.practicum.ewmservice.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewmservice.comment.dto.CommentDto;
import ru.practicum.ewmservice.comment.model.Comment;
import ru.practicum.ewmservice.user.mapper.UserMapper;


@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    CommentDto toCommentDto(Comment comment);
}
