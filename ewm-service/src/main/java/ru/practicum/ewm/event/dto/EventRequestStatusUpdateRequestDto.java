package ru.practicum.ewm.event.dto;

import lombok.Data;
import ru.practicum.ewm.event.model.StateStatusRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequestDto {

    @NotNull(message = "Ошибка! ids запросов, к которым относится событие, не могут быть пустыми.")
    private List<Long> requestIds;

    @NotNull(message = "Ошибка! Статус обновления запросов, к которым относится событие, не может быть пустым.")
    private StateStatusRequest status;

}
