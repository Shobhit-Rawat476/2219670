package com.AffordMedical.OnlineAssessment.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class ShortenUrlResponse {
    private String shortUrl;
    private String originalUrl;
    private String shortCode;
    private int validityMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    public ShortenUrlResponse() {}

    public ShortenUrlResponse(String shortUrl, String originalUrl, String shortCode,
                              int validityMinutes, LocalDateTime expiresAt) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.validityMinutes = validityMinutes;
        this.expiresAt = expiresAt;
    }
}
