package ru.practicum.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClient {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final RestTemplate rest;

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        String serverUrl = "http://ewm-stat-server:9090";

        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }

    public static List<ViewStats> getStats(LocalDateTime startTime, LocalDateTime endTime, @Nullable String[] uris, @Nullable Boolean unique) {
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
        return makeAndSendGetStatsRequest(HttpMethod.GET, sb.toString(), parameters, null);
    }

    public static ResponseEntity<String> postHit(EndpointHitDto hit) {
        ResponseEntity<String> responseEntity = makeAndSendPostHitRequest(HttpMethod.POST, "/hit", null, hit);
        return responseEntity;
    }

    private static <T> List<ViewStats> makeAndSendGetStatsRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<List<ViewStats>> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<ViewStats>>() {
                }, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<ViewStats>>() {
                });
            }
        } catch (HttpStatusCodeException e) {
            return null;
        }
        return ewmServerResponse.getBody();
    }

    private static <T> ResponseEntity<String> makeAndSendPostHitRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<String> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, String.class, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, String.class);
            }
        } catch (HttpStatusCodeException e) {
            return null;
        }
        return ewmServerResponse;
    }

    private static HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}