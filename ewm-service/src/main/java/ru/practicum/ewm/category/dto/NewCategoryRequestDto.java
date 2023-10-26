package ru.practicum.ewm.category.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NewCategoryRequestDto {

    @NotBlank(message = "Ошибка! Название категории не может быть пустым.")
    @Size(max = 50, message = "Ошибка! Название категории может содержать максимум 50 символов.")
    private String name;

}
