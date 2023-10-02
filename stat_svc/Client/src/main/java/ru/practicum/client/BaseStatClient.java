package ru.practicum.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointStats;

import java.util.List;
import java.util.Map;

public class BaseStatClient {
    protected static final RestTemplate rest = new RestTemplateBuilder()
            //.uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090"))
            .uriTemplateHandler(new DefaultUriBuilderFactory("http://ewm-stat-server:9090"))
            .build();

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    protected static <T> ResponseEntity<String> post(String path, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendPostHitRequest(HttpMethod.POST, path, parameters, body);
    }

    private static <T> ResponseEntity<String> makeAndSendPostHitRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, /*Nullable */T body) {
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

    protected static ResponseEntity<List<EndpointStats>> get(String path, @Nullable Map<String, Object> parameters) {
        return makeAndSendGetStatsRequest(HttpMethod.GET, path, parameters, null);
    }

    private static <T> ResponseEntity<List<EndpointStats>> makeAndSendGetStatsRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<List<EndpointStats>> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<EndpointStats>>() {
                }, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<EndpointStats>>() {
                });
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