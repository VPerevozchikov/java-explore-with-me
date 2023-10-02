package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsClient extends BaseStatClient {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ResponseEntity<String> postHit(EndpointHitDto hit) {
        return post("/hit", null, hit);
    }

    public static List<EndpointStats> getStats(LocalDateTime startTime, LocalDateTime endTime, @Nullable String[] uris, @Nullable Boolean unique) {
        String startString = startTime.format(TIME_FORMAT);
        String endString = endTime.format(TIME_FORMAT);
        String startEncoded = URLEncoder.encode(startString, StandardCharsets.UTF_8);
        String endEncoded = URLEncoder.encode(endString, StandardCharsets.UTF_8);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", startEncoded);
        parameters.put("end", endEncoded);

        StringBuilder sb = new StringBuilder();
        sb.append("/stats?start={start}&end={end}");
        if (uris != null) {
            parameters.put("uris", uris);
            sb.append("&uris={uris}");
        }
        if (unique != null) {
            parameters.put("unique", unique);
            sb.append("&unique={unique}");
        }
        return get(sb.toString(), parameters).getBody();
    }

    public static Map<Integer, Long> getMapIdViews(Collection<Integer> eventsId) {
        if (eventsId == null || eventsId.isEmpty()) {
            return new HashMap<>();
        }
        List<String> eventUris = eventsId.stream()
                .map(i -> "/events/" + i)
                .collect(Collectors.toList());

        String[] uriArray = new String[eventUris.size()];
        eventUris.toArray(uriArray);

        List<EndpointStats> endpointStatsList = getStats(LocalDateTime.of(1970, 01, 01, 01, 01), LocalDateTime.now(), uriArray, true);

        if (endpointStatsList == null || endpointStatsList.isEmpty()) {
            return eventsId.stream()
                    .collect(Collectors.toMap(e -> e, e -> 0L));
        }
        Map<Integer, Long> idViewsMap = endpointStatsList.stream()
                .collect(Collectors.toMap(e -> {
                            String[] splitUri = e.getUri().split("/");
                            return Integer.valueOf(splitUri[splitUri.length - 1]);
                        },
                        EndpointStats::getHits));
        return idViewsMap;
    }
}