package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestParamDto {
    private final String start;
    private final String end;
    private final String[] uris;
    private final boolean unique;

}