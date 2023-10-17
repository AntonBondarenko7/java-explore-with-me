package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.service.StatsServerService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.utils.Constants.PATTERN_FOR_DATETIME;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsServerController {

    private final StatsServerService statsServerService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit saveHit(@RequestBody EndpointHit endpointHit) {
        log.info("Добавлена новая информация о запросе: {}", endpointHit);
        return statsServerService.saveEndpointHit(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getAllStats(
            @RequestParam @DateTimeFormat(pattern = PATTERN_FOR_DATETIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = PATTERN_FOR_DATETIME) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получена статистика по посещениям start = {}, end = {}, uris = {}, unique = {}.",
                start, end, uris, unique);
        return statsServerService.getAllStats(start, end, uris, unique);
    }

}
