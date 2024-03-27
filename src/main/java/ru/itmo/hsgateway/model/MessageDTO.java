package ru.itmo.hsgateway.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {

    private final LocalDateTime timestamp;
    private final String summary;
    private final String message;
    private final String token;

    public MessageDTO(String summary, String message, String token) {
        this.timestamp = LocalDateTime.now();
        this.summary = summary;
        this.message = message;
        this.token = token;
    }
}
