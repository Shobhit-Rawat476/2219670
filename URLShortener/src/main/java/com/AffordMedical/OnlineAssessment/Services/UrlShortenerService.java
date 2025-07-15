package com.AffordMedical.OnlineAssessment.Services;

import com.AffordMedical.OnlineAssessment.Entities.UrlEntity;
import com.AffordMedical.OnlineAssessment.Exception.UrlShortenerException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
public class UrlShortenerService {

    @Autowired
    private LoggingService loggingService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // In-memory storage (use database in production)
    private final ConcurrentHashMap<String, UrlEntity> urlDatabase = new ConcurrentHashMap<>();

    private static final String PACKAGE_NAME = "com.AffordMedical.OnlineAssessment.Services";
    private static final String STACK_NAME = "url-shortener-service";

    public UrlEntity shortenUrl(String originalUrl, Integer validityMinutes) {
        try {
            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Starting URL shortening process for: " + originalUrl);

            // Validate URL
            validateUrl(originalUrl);

            // Set default validity if not provided
            validityMinutes = validityMinutes != null ? validityMinutes : 30;

            // Generate short code
            String shortCode = generateShortCode(originalUrl);

            // Check if short code already exists
            if (urlDatabase.containsKey(shortCode)) {
                UrlEntity existing = urlDatabase.get(shortCode);
                if (!existing.isExpired()) {
                    loggingService.log(STACK_NAME, "WARN", PACKAGE_NAME,
                            "Short code already exists and is still valid: " + shortCode);
                    throw new UrlShortenerException("Short code collision occurred", "COLLISION_ERROR");
                }
            }

            // Create URL entity
            UrlEntity urlEntity = new UrlEntity(shortCode, originalUrl, validityMinutes);
            urlDatabase.put(shortCode, urlEntity);

            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "URL shortened successfully. Short code: " + shortCode);

            return urlEntity;

        } catch (Exception e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Error shortening URL: " + e.getMessage());
            throw e;
        }
    }

    public String getOriginalUrl(String shortCode) {
        try {
            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Retrieving original URL for short code: " + shortCode);

            UrlEntity urlEntity = urlDatabase.get(shortCode);

            if (urlEntity == null) {
                loggingService.log(STACK_NAME, "WARN", PACKAGE_NAME,
                        "Short code not found: " + shortCode);
                throw new UrlShortenerException("Short URL not found", "NOT_FOUND");
            }

            if (!urlEntity.isActive()) {
                loggingService.log(STACK_NAME, "WARN", PACKAGE_NAME,
                        "Short code is inactive: " + shortCode);
                throw new UrlShortenerException("Short URL is inactive", "INACTIVE");
            }

            if (urlEntity.isExpired()) {
                loggingService.log(STACK_NAME, "WARN", PACKAGE_NAME,
                        "Short code has expired: " + shortCode);
                // Clean up expired URL
                urlDatabase.remove(shortCode);
                throw new UrlShortenerException("Short URL has expired", "EXPIRED");
            }

            // Increment click count
            urlEntity.setClickCount(urlEntity.getClickCount() + 1);

            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Successful redirect for short code: " + shortCode + " (clicks: " + urlEntity.getClickCount() + ")");

            return urlEntity.getOriginalUrl();

        } catch (Exception e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Error retrieving original URL: " + e.getMessage());
            throw e;
        }
    }

    public UrlEntity getUrlStats(String shortCode) {
        UrlEntity urlEntity = urlDatabase.get(shortCode);
        if (urlEntity == null) {
            throw new UrlShortenerException("Short URL not found", "NOT_FOUND");
        }
        return urlEntity;
    }

    private void validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new UrlShortenerException("Invalid URL format", "INVALID_URL");
        }
    }

    private String generateShortCode(String originalUrl) {
        try {
            // Create hash of the URL with timestamp for uniqueness
            String input = originalUrl + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            // Convert to base64 and take first 8 characters
            String base64 = Base64.getEncoder().encodeToString(hash);
            return base64.substring(0, 8).replace("+", "a").replace("/", "b");

        } catch (NoSuchAlgorithmException e) {
            throw new UrlShortenerException("Error generating short code", "GENERATION_ERROR");
        }
    }
}
