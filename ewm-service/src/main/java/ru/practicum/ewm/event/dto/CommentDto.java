package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewm.event.model.CommentStatus;

import java.time.LocalDateTime;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@Data
public class CommentDto {

    private Long id;

    private Long authorId;

    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_FOR_DATETIME)
    private LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_FOR_DATETIME)
    private LocalDateTime editedOn;

    private CommentStatus status;

}
