package ru.itmo.hsgateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorDTO {
    private final String timestamp;
    private final String error;
    private final String message;
    private List<ViolationDTO> violations;

    public ErrorDTO(String timestamp, String message, String error) {
        this.timestamp = timestamp;
        this.message = message;
        this.error = error;
    }
}
