package com.AffordMedical.OnlineAssessment.Exception;

import com.AffordMedical.OnlineAssessment.DTO.ApiResponse;
import com.AffordMedical.OnlineAssessment.Services.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private LoggingService loggingService;

    private static final String PACKAGE_NAME = "com.example.urlshortener.exception";
    private static final String STACK_NAME = "global-exception-handler";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        loggingService.log(STACK_NAME, "WARN", PACKAGE_NAME,
                "Validation failed: " + errors.toString());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed", "VALIDATION_ERROR"));
    }

    @ExceptionHandler(UrlShortenerException.class)
    public ResponseEntity<ApiResponse<Void>> handleUrlShortenerException(UrlShortenerException ex) {

        loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                "URL Shortener Exception: " + ex.getMessage());

        HttpStatus status = getStatusFromErrorCode(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

        loggingService.log(STACK_NAME, "ERROR", PACKAGE_NAME,
                "Unexpected error: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
    }

    private HttpStatus getStatusFromErrorCode(String errorCode) {
        switch (errorCode) {
            case "NOT_FOUND":
            case "EXPIRED":
                return HttpStatus.NOT_FOUND;
            case "INVALID_URL":
            case "VALIDATION_ERROR":
                return HttpStatus.BAD_REQUEST;
            case "INACTIVE":
                return HttpStatus.GONE;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
