package ru.practicum.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private String status;
    private String timeStamp;
}