package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.CommentMapper;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.CommentRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotSavedException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.StateRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.util.Stats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewm.utils.Constants.FORMATTER_FOR_DATETIME;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final Stats stats;

    @Transactional(readOnly = true)
    public List<EventDto> getAllEventsByAdmin(
            List<Long> users, List<StateEvent> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        List<Event> events = eventRepository.getAllEventsByAdmin(users, states,
                categories, rangeStart, rangeEnd, page);
        Map<Long, Long> views = returnMapViewStats(events, rangeStart, rangeEnd);
        List<EventDto> eventDtos = EventMapper.INSTANCE.convertEventListToEventDtoList(events);

        eventDtos = eventDtos.stream()
                .peek(dto -> dto.setConfirmedRequests(
                        requestRepository.countByEventIdAndStatus(dto.getId(), StateRequest.CONFIRMED)))
                .peek(dto -> dto.setViews(views.getOrDefault(dto.getId(), 0L)))
                .collect(Collectors.toList());

        return eventDtos;
    }

    public EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = findEventById(eventId);
        if (updateEventAdminRequestDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequestDto.getAnnotation());
        }
        if (updateEventAdminRequestDto.getCategory() != null) {
            Category category = categoryService.findCategoryById(updateEventAdminRequestDto.getCategory());
            event.setCategory(category);
        }
        if (updateEventAdminRequestDto.getDescription() != null) {
            event.setDescription(updateEventAdminRequestDto.getDescription());
        }
        if (updateEventAdminRequestDto.getEventDate() != null &&
                LocalDateTime.now().plusHours(1).isAfter(updateEventAdminRequestDto.getEventDate())) {
            throw new ValidationException(String.format("Дата начала изменяемого события должна быть не ранее, " +
                            "чем за час от даты публикации, eventId = %s, UpdateEventAdminRequestDto: %s.",
                    eventId, updateEventAdminRequestDto));
        }
        if (updateEventAdminRequestDto.getLocation() != null) {
            Location location = findLocation(updateEventAdminRequestDto.getLocation());
            event.setLocation(location);
        }
        if (updateEventAdminRequestDto.getPaid() != null) {
            event.setPaid(updateEventAdminRequestDto.getPaid());
        }
        if (updateEventAdminRequestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequestDto.getParticipantLimit());
        }
        if (updateEventAdminRequestDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequestDto.getRequestModeration());
        }
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (updateEventAdminRequestDto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) &&
                    !event.getState().equals(StateEvent.PENDING)) {
                throw new ConflictException(String.format("Событие можно публиковать только, если оно в состоянии " +
                                "ожидания публикации, eventId = %s, UpdateEventAdminRequestDto: %s.",
                        eventId, updateEventAdminRequestDto));
            }
            if (updateEventAdminRequestDto.getStateAction().equals(StateActionAdmin.REJECT_EVENT) &&
                    event.getState().equals(StateEvent.PUBLISHED)) {
                throw new ConflictException(String.format("событие можно отклонить только, если оно еще " +
                                "не опубликовано, eventId = %s, UpdateEventAdminRequestDto: %s.",
                        eventId, updateEventAdminRequestDto));
            }

            switch (updateEventAdminRequestDto.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(StateEvent.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(StateEvent.CANCELED);
                    break;
            }
        }
        if (updateEventAdminRequestDto.getTitle() != null) {
            event.setTitle(updateEventAdminRequestDto.getTitle());
        }

        try {
            return EventMapper.INSTANCE.toEventDto(eventRepository.saveAndFlush(event));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Событие с id = " + eventId + ", не было обновлено: " +
                    updateEventAdminRequestDto);
        }
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEventsByUser(Long userId, Integer from, Integer size) {
        userService.findUserById(userId);
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return EventMapper.INSTANCE.convertEventListToEventShortDtoList(
                eventRepository.findByInitiatorId(userId, page));
    }

    public EventDto saveEvent(Long userId, NewEventRequestDto newEventRequestDto) {
        User user = userService.findUserById(userId);
        Category category = categoryService.findCategoryById(newEventRequestDto.getCategory());
        if (!newEventRequestDto.getEventDate().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                    "Value: " + newEventRequestDto.getEventDate());
        }
        Location location = findLocation(newEventRequestDto.getLocation());

        Event event = EventMapper.INSTANCE.toEventFromNewDto(newEventRequestDto, user, category, location);
        event.setConfirmedRequests(0L);
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setPublishedOn(LocalDateTime.now());
        event.setState(StateEvent.PENDING);

        try {
            event = eventRepository.save(event);
            EventDto eventDto = EventMapper.INSTANCE.toEventDto(event);
            eventDto.setViews(0L);
            return eventDto;
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Событие не было создано: " + newEventRequestDto);
        }
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long userId, Long eventId) {
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        checkEventInitiator(event, userId);

        List<String> uris = List.of("/events/" + eventId);
        List<ViewStats> viewStats = stats.statsClient.getAllStats(
                LocalDateTime.now().minusYears(100).format(FORMATTER_FOR_DATETIME),
                LocalDateTime.now().plusYears(100).format(FORMATTER_FOR_DATETIME), uris, true);

        EventDto eventDto = EventMapper.INSTANCE.toEventDto(event);
        eventDto.setConfirmedRequests(requestRepository
                .countByEventIdAndStatus(eventId, StateRequest.CONFIRMED));
        eventDto.setViews(viewStats.isEmpty() ? 0L : viewStats.get(0).getHits());

        return eventDto;
    }

    public EventDto updateEvent(Long userId, Long eventId, UpdateEventUserRequestDto updateEventUserRequest) {
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        checkEventInitiator(event, userId);

        if (event.getState().equals(StateEvent.PUBLISHED)) {
            throw new ConflictException(String.format("Событие не должно быть опубликовано, userId = %s, " +
                    "eventId = %s, updateEventUserRequest: %s.", userId, eventId, updateEventUserRequest));
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryService.findCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().plusHours(2).isBefore(LocalDateTime.now())) {
                throw new ValidationException(String.format("Дата и время на которые намечено событие " +
                        "не может быть раньше, чем через два часа от текущего момента, userId = %s, eventId = %s, " +
                        "updateEventUserRequest: %s.", userId, eventId, updateEventUserRequest));
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = findLocation(updateEventUserRequest.getLocation());
            event.setLocation(location);
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (!event.getState().equals(StateEvent.PENDING) && !event.getState().equals(StateEvent.CANCELED)) {
                throw new ConflictException(String.format("Изменить можно только отмененные события или события " +
                        "в состоянии ожидания модерации, userId = %s, eventId = %s, " +
                        "updateEventUserRequest: %s.", userId, eventId, updateEventUserRequest));
            }
            if (updateEventUserRequest.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
                event.setState(StateEvent.CANCELED);
            }
            if (updateEventUserRequest.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                event.setState(StateEvent.PENDING);
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        try {
            return EventMapper.INSTANCE.toEventDto(eventRepository.saveAndFlush(event));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Событие с id = " + eventId + ", userId = " + userId + ", " +
                    "не было обновлено: " + updateEventUserRequest);
        }
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllRequestsOfEventByUser(Long userId, Long eventId) {
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        checkEventInitiator(event, userId);

        return ParticipationRequestMapper.INSTANCE.convertParticipationRequestToDtoList(
                requestRepository.findAllByEventId(eventId));
    }

    public EventRequestStatusUpdateResponseDto updateAllRequestsOfEventByUser(
            Long userId, Long eventId, EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequest) {
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        checkEventInitiator(event, userId);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("У события лимит заявок равен 0 или отключена пре-модерация, userId = "
                    + userId + ", eventId = " + eventId + ", eventRequestStatusUpdateRequest: "
                    + eventRequestStatusUpdateRequest);
        }
        long confirmedLimit = requestRepository.countByEventIdAndStatus(eventId, StateRequest.CONFIRMED);
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedLimit) {
            throw new ConflictException("Нельзя подтвердить заявку, так как достигнут лимит по заявкам " +
                    "на данное событие, userId = " + userId + ", eventId = " + eventId +
                    ", eventRequestStatusUpdateRequest: " + eventRequestStatusUpdateRequest);
        }

        List<ParticipationRequest> requestsToUpdate = requestRepository
                .findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());
        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestsToUpdate) {
            if (!request.getStatus().equals(StateRequest.PENDING)) {
                throw new ConflictException("Нельзя отменить уже принятую заявку на участие, userId = " + userId
                        + ", eventId = " + eventId + ", eventRequestStatusUpdateRequest: "
                        + eventRequestStatusUpdateRequest);
            }
            if (!request.getEvent().getId().equals(eventId)) {
                rejected.add(request);
                continue;
            }

            switch (eventRequestStatusUpdateRequest.getStatus()) {
                case CONFIRMED:
                    if (confirmedLimit < event.getParticipantLimit()) {
                        request.setStatus(StateRequest.CONFIRMED);
                        confirmedLimit++;
                        confirmed.add(request);
                    } else {
                        request.setStatus(StateRequest.REJECTED);
                        rejected.add(request);
                    }
                    break;

                case REJECTED:
                    request.setStatus(StateRequest.REJECTED);
                    rejected.add(request);
                    break;
            }
        }

        requestRepository.saveAll(requestsToUpdate);
        EventRequestStatusUpdateResponseDto result = new EventRequestStatusUpdateResponseDto();
        result.setConfirmedRequests(ParticipationRequestMapper.INSTANCE
                .convertParticipationRequestToDtoList(confirmed));
        result.setRejectedRequests(ParticipationRequestMapper.INSTANCE
                .convertParticipationRequestToDtoList(rejected));

        return result;
    }

    public List<EventShortDto> getAllEvents(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, SortSearch sort, Integer from, Integer size, HttpServletRequest request) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException(String.format("Дата и время не позже которых должно произойти событие " +
                            "должно быть позже даты и времени  не раньше которых должно произойти событие, " +
                            "text = %s, categories = %s, paid = %s, rangeStart = %s, rangeEnd = %s, " +
                            "onlyAvailable = %s, sort = %s, from = %s, size = %s.",
                    text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size));
        }

        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-service");
        endpointHit.setUri(request.getRequestURI());
        endpointHit.setIp(request.getRemoteAddr());
        endpointHit.setTimestamp(LocalDateTime.now());
        stats.statsClient.saveHit(endpointHit);

        if (categories != null && categories.size() == 1 && categories.get(0).equals(0L)) {
            categories = null;
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        List<Event> events = eventRepository.getAllEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, page);
        if (events != null) {
            for (Event event : events) {
                endpointHit.setUri("/events/" + event.getId());
                endpointHit.setTimestamp(LocalDateTime.now());
                stats.statsClient.saveHit(endpointHit);
            }
        }
        Map<Long, Long> views = returnMapViewStats(events, rangeStart, rangeEnd);
        List<EventShortDto> eventShortDtos = EventMapper.INSTANCE.convertEventListToEventShortDtoList(events);

        eventShortDtos.stream()
                .peek(dto -> dto.setConfirmedRequests(
                        requestRepository.countByEventIdAndStatus(dto.getId(), StateRequest.CONFIRMED)))
                .peek(dto -> dto.setViews(views.getOrDefault(dto.getId(), 0L)))
                .collect(Collectors.toList());

        if (sort.equals(SortSearch.VIEWS)) {
            eventShortDtos.sort((e1, e2) -> (int) (e2.getViews() - e1.getViews()));
        }
        return eventShortDtos;
    }

    public EventDto getPublicEventById(Long eventId, HttpServletRequest request) {
        Event event = findEventById(eventId);
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new NotFoundException("Событие с id = " + eventId + " должно быть опубликовано.");
        }

        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-service");
        endpointHit.setUri(request.getRequestURI());
        endpointHit.setIp(request.getRemoteAddr());
        endpointHit.setTimestamp(LocalDateTime.now());
        stats.statsClient.saveHit(endpointHit);

        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStats> viewStats = stats.statsClient.getAllStats(
                LocalDateTime.now().minusYears(100).format(FORMATTER_FOR_DATETIME),
                LocalDateTime.now().plusYears(100).format(FORMATTER_FOR_DATETIME), uris, true);

        EventDto eventDto = EventMapper.INSTANCE.toEventDto(event);
        eventDto.setConfirmedRequests(requestRepository
                .countByEventIdAndStatus(event.getId(), StateRequest.CONFIRMED));
        eventDto.setViews(viewStats.isEmpty() ? 0L : viewStats.get(0).getHits());

        return eventDto;
    }

    public Map<Long, Long> returnMapViewStats(List<Event> events, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        List<ViewStats> viewStatList = stats.statsClient.getAllStats(rangeStart.format(FORMATTER_FOR_DATETIME),
                rangeEnd.format(FORMATTER_FOR_DATETIME), uris, true);

        Map<Long, Long> views = new HashMap<>();
        for (ViewStats viewStats : viewStatList) {
            Long id = Long.parseLong(viewStats.getUri().split("/events/")[1]);
            views.put(id, views.getOrDefault(id, 0L) + 1);
        }
        return views;
    }

    @Transactional
    public Location findLocation(LocationDto locationDto) {
        Location location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        return location != null ? location : locationRepository.save(LocationMapper.INSTANCE.toLocation(locationDto));
    }

    public void checkEventInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId +
                    " не является инициатором события с id = " + event.getId());
        }
    }

    public Event findEventById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));

        event.setComments(
                commentRepository.findAllByEventIdAndStatusOrderByCreatedOnDesc(eventId));

        return event;
    }

    public CommentDto saveComment(Long userId, Long eventId, NewCommentRequestDto newCommentRequestDto) {
        userService.findUserById(userId);
        findEventById(eventId);

        Comment comment = CommentMapper.INSTANCE.toCommentFromNewDto(newCommentRequestDto, eventId, userId);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING_MODERATION);

        try {
            return CommentMapper.INSTANCE.toCommentDto(commentRepository.save(comment));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Не удалось сохранить комментарий");
        }

    }

    public CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с идентификатором " + commentId + " не найден."));

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }

        if (comment.getStatus().equals(CommentStatus.PENDING_MODERATION)) {
            comment.setStatus(CommentStatus.MODERATED);
        }

        try {
            return CommentMapper.INSTANCE.toCommentDto(commentRepository.save(comment));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Не удалось обновить комментарий");
        }

    }

    public CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с идентификатором " + commentId + " не найден."));

        userService.findUserById(userId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Пользователь с идентификатором " + userId +
                    " не является автором комментария.");
        }

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }

        if (comment.getStatus().equals(CommentStatus.MODERATED)) {
            comment.setStatus(CommentStatus.PENDING_MODERATION);
        }

        comment.setEditedOn(LocalDateTime.now());

        try {
            return CommentMapper.INSTANCE.toCommentDto(commentRepository.save(comment));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Не удалось обновить комментарий");
        }

    }

    public Boolean deleteCommentById(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с идентификатором " + commentId + " не найден."));

        userService.findUserById(userId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ValidationException("Пользователь с идентификатором " + userId +
                    " не является автором комментария.");
        }

        return commentRepository.deleteByIdWithReturnedLines(commentId) >=0;

    }

    public List<CommentDto> getCommentsToModerate() {
        List<Comment> comments = commentRepository.findAllByStatusOrderByCreatedOnAsc(CommentStatus.PENDING_MODERATION);

        return CommentMapper.INSTANCE.convertCommentListToCommentDTOList(comments);
    }

}
