package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.CommentDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.UpdateCommentDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.event.model.StateEvent;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventDto> getAllEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<StateEvent> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_FOR_DATETIME) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_FOR_DATETIME) LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен список событий, users = {}, states = {}, categories = {}, rangeStart = {}, rangeEnd = {}, " +
                "from = {}, size = {}.", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getAllEventsByAdmin(
                users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @Validated
    public EventDto updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequestDto updateEventAdminRequest) {
        EventDto eventDto = eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
        log.info("Обновлено событие админом, с id = {}: {}.", eventId, eventDto);
        return eventDto;
    }

    @PatchMapping("/comments/{commentId}")
    @Validated
    public CommentDto updateCommentByAdmin(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        CommentDto commentDto = eventService.updateCommentByAdmin(commentId, updateCommentDto);
        log.info("Обновлен комментарий админом, с id = {}: {}.", commentId, commentDto);
        return commentDto;
    }
}
