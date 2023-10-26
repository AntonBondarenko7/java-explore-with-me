package ru.practicum.ewm.compilation.dto;

import lombok.Data;

@Data
public class CompilationDto {

    private Long id;

    private Set<EventShortDto> events;

    private Boolean pinned;

    private String title;

}
