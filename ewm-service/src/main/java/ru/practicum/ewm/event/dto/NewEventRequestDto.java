package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@Data
public class NewEventRequestDto {

    @NotBlank(message = "Ошибка! Краткое описание события не может быть пустым.")
    @Size(min = 20, max = 2000, message =
            "Ошибка! Краткое описание события может содержать минимум 20, максимум 2000 символов.")
    private String annotation;

    @NotNull(message = "Ошибка! id категории, к которой относится событие, не может быть пустым.")
    private Long category;

    @NotBlank(message = "Ошибка! Полное описание события не может быть пустым.")
    @Size(min = 20, max = 7000, message =
            "Ошибка! Полное описание события может содержать минимум 20, максимум 7000 символов.")
    private String description;

    @NotNull(message = "Ошибка! Дата и время, на которые намечено событие, не могут быть пустыми.")
    @FutureOrPresent(message = "Ошибка! Дата события должна еще не наступить.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN_FOR_DATETIME)
    private LocalDateTime eventDate;

    @NotNull(message = "Ошибка! Широта и долгота места проведения события не могут быть пустыми.")
    private LocationDto location;

    private Boolean paid = false;

    private Integer participantLimit = 0;


    private Boolean requestModeration = true;

    @NotBlank(message = "Ошибка! Заголовок события не может быть пустым.")
    @Size(min = 3, max = 120, message =
            "Ошибка! Заголовок события может содержать минимум 3, максимум 120 символов.")
    private String title;

}
