package ru.practicum.ewm.compilation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class NewCompilationRequestDto {

    private Set<Long> events;

    private Boolean pinned = false;

    @NotBlank(message = "Ошибка! Заголовок подборки не может быть пустым.")
    @Size(min = 1, max = 50, message =
            "Ошибка! Заголовок подборки может содержать минимум 1, максимум 50 символов.")
    private String title;

}
