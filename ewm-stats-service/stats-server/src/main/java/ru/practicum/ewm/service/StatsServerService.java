package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.exception.HitNotSavedException;
import ru.practicum.ewm.mapper.StatsServerMapper;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.repository.StatsServerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServerService {

    private final StatsServerRepository statsServerRepository;

    @Transactional
    public EndpointHit saveEndpointHit(EndpointHit endpointHit) {
        try {
            Hit hit = statsServerRepository.save(StatsServerMapper.INSTANCE.toHit(endpointHit));
            return StatsServerMapper.INSTANCE.toEndpointHit(hit);
        } catch (DataIntegrityViolationException e) {
            throw new HitNotSavedException(endpointHit.toString());
        }
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            if (uris == null) {
                return statsServerRepository.getAllUniqueStats(start, end);
            } else {
                return statsServerRepository.getAllUniqueStatsWithUris(start, end, uris);
            }
        } else {
            if (uris == null) {
                return statsServerRepository.getAllStats(start, end);
            } else {
                return statsServerRepository.getAllStatsWithUris(start, end, uris);
            }
        }
    }

}
