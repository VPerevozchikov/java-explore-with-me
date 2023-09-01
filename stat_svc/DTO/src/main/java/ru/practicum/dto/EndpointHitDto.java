package ru.practicum.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class EndpointHitDto {
    private int id;
    @NotBlank
    @Size(min = 1, max = 100)
    private String app;
    @NotBlank
    @Size(min = 1, max = 40)
    private String uri;
    @NotBlank
    @Size(min = 1, max = 40)
    private String ip;
    @NotBlank
    @Size(min = 1, max = 40)
    private String timestamp;

    public EndpointHitDto(String app, String uri, String ip) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
    }
}
