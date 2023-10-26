package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationRequestDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.NotSavedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationDto saveCompilation(NewCompilationRequestDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(newCompilationDto.getEvents()));
        }

        try {
            Compilation compilation = compilationRepository.save(
                    CompilationMapper.INSTANCE.toCompilationFromNewDto(newCompilationDto, events));
            return CompilationMapper.INSTANCE.toCompilationDto(compilation);
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Подборка событий не была создана: " + newCompilationDto);
        }
    }


    public void deleteCompilationById(Long compId) {
        findCompilationById(compId);
        try {
            compilationRepository.deleteById(compId);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Подборка с ид = " + compId + " не может быть удалена.");
        }
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto updateCompilationRequest) {
        Compilation compilation = findCompilationById(compId);

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents()));
            compilation.setEvents(events);
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        try {
            return CompilationMapper.INSTANCE.toCompilationDto(compilationRepository.saveAndFlush(compilation));
        } catch (DataIntegrityViolationException e) {
            throw new NotSavedException("Подборка событий с id = " + compId +
                    " не была обновлена: " + updateCompilationRequest);
        }
    }

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));

        if (pinned == null) {
            return CompilationMapper.INSTANCE.convertCompilationListToCompilationDTOList(
                    compilationRepository.findAll(page).getContent());
        } else {
            return CompilationMapper.INSTANCE.convertCompilationListToCompilationDTOList(
                    compilationRepository.findAllByPinned(pinned, page));
        }
    }

    public CompilationDto getCompilationById(Long compId) {
        return CompilationMapper.INSTANCE.toCompilationDto(findCompilationById(compId));
    }

    private Compilation findCompilationById(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий с идентификатором " + compId + " не найдена."));
    }

}
