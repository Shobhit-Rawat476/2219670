package com.AffordMedical.OnlineAssessment.Controller;

import com.AffordMedical.OnlineAssessment.DTO.ApiResponse;
import com.AffordMedical.OnlineAssessment.DTO.ShortenUrlRequest;
import com.AffordMedical.OnlineAssessment.DTO.ShortenUrlResponse;
import com.AffordMedical.OnlineAssessment.Entities.UrlEntity;
import com.AffordMedical.OnlineAssessment.Exception.UrlShortenerException;
import com.AffordMedical.OnlineAssessment.Services.LoggingService;
import com.AffordMedical.OnlineAssessment.Services.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/url")
@Validated
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private LoggingService loggingService;

    private static final String PACKAGE_NAME = "com.example.urlshortener.controller";
    private static final String STACK_NAME = "url-shortener-controller";

    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<ShortenUrlResponse>> shortenUrl(
            @Valid
            @RequestBody
            ShortenUrlRequest request) {

        try {
            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Received URL shortening request for: " + request.getUrl());

            UrlEntity urlEntity = urlShortenerService.shortenUrl(
                    request.getUrl(), request.getValidityMinutes());

            String shortUrl = urlShortenerService.getBaseUrl() + "/api/url/" + urlEntity.getShortCode();

            ShortenUrlResponse response = new ShortenUrlResponse(
                    shortUrl,
                    urlEntity.getOriginalUrl(),
                    urlEntity.getShortCode(),
                    request.getValidityMinutes(),
                    urlEntity.getExpiresAt()
            );

            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "URL shortened successfully. Short URL: " + shortUrl);

            return ResponseEntity.ok(ApiResponse.success(response, "URL shortened successfully"));

        } catch (UrlShortenerException e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "URL shortening failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));

        } catch (Exception e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Unexpected error during URL shortening: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode,
                                      HttpServletResponse response) throws IOException {

        try {
            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Redirect request for short code: " + shortCode);

            String originalUrl = urlShortenerService.getOriginalUrl(shortCode);

            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Redirecting to: " + originalUrl);

            response.sendRedirect(originalUrl);

        } catch (UrlShortenerException e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Redirect failed: " + e.getMessage());

            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");

        } catch (Exception e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Unexpected error during redirect: " + e.getMessage());

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<ApiResponse<UrlEntity>> getUrlStats(@PathVariable String shortCode) {

        try {
            loggingService.log(STACK_NAME, "INFO", PACKAGE_NAME,
                    "Stats request for short code: " + shortCode);

            UrlEntity urlEntity = urlShortenerService.getUrlStats(shortCode);

            return ResponseEntity.ok(ApiResponse.success(urlEntity, "Stats retrieved successfully"));

        } catch (UrlShortenerException e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Stats retrieval failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));

        } catch (Exception e) {
            loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                    "Unexpected error during stats retrieval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
}