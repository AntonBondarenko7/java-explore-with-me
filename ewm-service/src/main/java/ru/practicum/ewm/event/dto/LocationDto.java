package ru.practicum.ewm.event.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LocationDto {

    private Long id;

    @NotNull(message = "Ошибка! Широта не может быть пустой.")
    private Float lat;

    @NotNull(message = "Ошибка! Долгота не может быть пустой.")
    private Float lon;

}
