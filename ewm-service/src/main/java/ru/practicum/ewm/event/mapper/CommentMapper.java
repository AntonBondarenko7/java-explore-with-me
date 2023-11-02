package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.event.dto.CommentDto;
import ru.practicum.ewm.event.dto.NewCommentRequestDto;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.util.List;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@Mapper
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(target = "authorId", source = "comment.authorId")
    @Mapping(target = "createdOn", source = "comment.createdOn", dateFormat = PATTERN_FOR_DATETIME)
    @Mapping(target = "editedOn", source = "comment.editedOn", dateFormat = PATTERN_FOR_DATETIME)
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "authorId", source = "authorId")
    Comment toCommentFromNewDto(NewCommentRequestDto commentDto,Long eventId, Long authorId);


    List<CommentDto> convertCommentListToCommentDTOList(List<Comment> list);
}
