package ru.practicum.ewm.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewm.request.model.StateRequest;

import java.time.LocalDateTime;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@Data
public class ParticipationRequestDto {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_FOR_DATETIME)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private StateRequest status;

}
