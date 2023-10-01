package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;
import ru.practicum.dto.RequestParamDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<String> postHit(@Valid @RequestBody EndpointHitDto hitDto) {
        log.info("Сохранение запроса {}", hitDto.getUri());
        statsService.saveHit(hitDto);
        return new ResponseEntity<>("Запрос сохранен", HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<EndpointStats> getStats(@RequestParam(name = "start") String start,
                                        @RequestParam(name = "end") String end,
                                        @RequestParam(name = "uris", required = false) String[] uris,
                                        @RequestParam(name = "unique", defaultValue = "false") boolean unique) {
        log.info("Запрос статистики по адресам {}", uris);
        RequestParamDto requestDto = new RequestParamDto(start, end, uris, unique);
        return statsService.getStats(requestDto);
    }

    @GetMapping("/hits")
    public List<EndpointHitDto> getAllHits() {
        log.info("Запрос полной статистики");
        return statsService.getAllHits();
    }
}