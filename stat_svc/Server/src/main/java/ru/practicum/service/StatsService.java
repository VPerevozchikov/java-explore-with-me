package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitMapper;
import ru.practicum.dto.ViewStats;
import ru.practicum.repository.StatsJpaRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsJpaRepository statsJpaRepository;

    @Autowired
    public StatsService(StatsJpaRepository statsJpaRepository) {
        this.statsJpaRepository = statsJpaRepository;
    }

    public void saveHit(EndpointHitDto hitDto) {
        statsJpaRepository.save(EndpointHitMapper.toHit(hitDto));
    }

    public List<EndpointHitDto> getAllHits() {
        return statsJpaRepository.findAll().stream()
                .map(EndpointHitMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ViewStats> getStats(String startFromQuery, String endFromQuery, String[] uris, boolean unique) {
        String startDecoded = URLDecoder.decode(startFromQuery, StandardCharsets.UTF_8);
        String endDecoded = URLDecoder.decode(endFromQuery, StandardCharsets.UTF_8);

        LocalDateTime start = LocalDateTime.parse(startDecoded, TIME_FORMAT);
        LocalDateTime end = LocalDateTime.parse(endDecoded, TIME_FORMAT);

        if (start.isAfter(end)) {
            throw new RuntimeException("Ошибка: время начала задано позже времени окончания.");
        }

        if (unique) {
            if (uris == null) {
                return statsJpaRepository.getStatsUnique(start, end);
            } else {
                return statsJpaRepository.getStatsUniqueWithUris(start, end, uris);
            }
        } else {
            if (uris == null) {
                return statsJpaRepository.getStatsNotUnique(start, end);
            } else {
                return statsJpaRepository.getStatsNotUniqueWithUris(start, end, uris);
            }
        }
    }
}