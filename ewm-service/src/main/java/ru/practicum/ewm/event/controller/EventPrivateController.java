package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEventsByUser(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен список событий, добавленных текущим пользователем, userId = {}, from = {}, size = {}.",
                userId, from, size);
        return eventService.getAllEventsByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public EventDto saveEvent(
            @PathVariable Long userId, @Valid @RequestBody NewEventRequestDto newEventRequestDto) {
        EventDto eventDto = eventService.saveEvent(userId, newEventRequestDto);
        log.info("Добавлено новое событие: {}", eventDto);
        return eventDto;
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(
            @PathVariable Long userId, @PathVariable Long eventId) {
        EventDto eventDto = eventService.getEventById(userId, eventId);
        log.info("Получено событие, добавленное текущим пользователем, с id = {} для userId = {}: {}.",
                eventId, userId, eventDto);
        return eventDto;
    }

    @PatchMapping("/{eventId}")
    @Validated
    public EventDto updateEvent(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequestDto updateEventUserRequest) {
        EventDto eventDto = eventService.updateEvent(userId, eventId, updateEventUserRequest);
        log.info("Обновлено событие, добавленное текущим пользователем, с id = {} для userId = {}: {}.",
                userId, eventId, eventDto);
        return eventDto;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestsOfEventByUser(
            @PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен список запросов на участие в событии текущего пользователя, userId = {}, eventId = {}.",
                userId, eventId);
        return eventService.getAllRequestsOfEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @Validated
    public EventRequestStatusUpdateResponseDto updateAllRequestsOfEventByUser(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequest) {
        EventRequestStatusUpdateResponseDto eventRequestStatusUpdateResults = eventService.updateAllRequestsOfEventByUser(userId, eventId, eventRequestStatusUpdateRequest);
        log.info("Изменён статус заявок на участие в событии текущего пользователя, с id = {} для eventId = {}: {}.",
                userId, eventId, eventRequestStatusUpdateResults);
        return eventRequestStatusUpdateResults;
    }

    @PostMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public CommentDto saveComment(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody NewCommentRequestDto newCommentRequestDto) {
        CommentDto commentDto = eventService.saveComment(userId, eventId, newCommentRequestDto);
        log.info("Создан новый комментарий: {}", commentDto);
        return commentDto;
    }

    @PatchMapping("/comments/{commentId}")
    @Validated
    public CommentDto updateCommentByUser(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        CommentDto commentDto = eventService.updateCommentByUser(userId, commentId, updateCommentDto);
        log.info("Пользователем с id = {} обновлен комментарий с id = {}: {}.", commentId, userId, commentDto);
        return commentDto;
    }

    @DeleteMapping("/comments/{commentId}")
    public Boolean deleteCommentById(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        log.info("Пользователем с id = {} удален комментарий с id = {}", userId, commentId);
        return eventService.deleteCommentById(commentId, userId);
    }

}
