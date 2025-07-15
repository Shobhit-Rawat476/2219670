package com.AffordMedical.OnlineAssessment.Exception;

public class UrlShortenerException extends RuntimeException {
    private String errorCode;

    public UrlShortenerException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UrlShortenerException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
