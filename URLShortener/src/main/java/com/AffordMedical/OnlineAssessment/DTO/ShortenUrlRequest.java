package com.AffordMedical.OnlineAssessment.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
@Data
public class ShortenUrlRequest {
    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    @Min(value = 1, message = "Validity must be at least 1 minute")
    private Integer validityMinutes = 30; // Default 30 minutes

    public ShortenUrlRequest() {}

    public ShortenUrlRequest(String url, Integer validityMinutes) {
        this.url = url;
        this.validityMinutes = validityMinutes != null ? validityMinutes : 30;
    }

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getValidityMinutes() { return validityMinutes; }
    public void setValidityMinutes(Integer validityMinutes) {
        this.validityMinutes = validityMinutes != null ? validityMinutes : 30;
    }
}
