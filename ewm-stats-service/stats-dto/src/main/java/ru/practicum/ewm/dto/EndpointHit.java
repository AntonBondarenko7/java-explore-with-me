package ru.practicum.ewm.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.utils.Constants;


@Data
public class EndpointHit {

    private Long id;

    private String app;

    private String uri;

    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.PATTERN_FOR_DATETIME)
    private String timestamp;

}
