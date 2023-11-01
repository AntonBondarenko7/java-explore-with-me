package ru.practicum.ewm.event.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UpdateCommentDto {

    @Size(min = 5, max = 256, message =
            "Ошибка! Количество символов в комментарии может содержать минимум 5, максимум 256 символов.")
    private String text;

}
