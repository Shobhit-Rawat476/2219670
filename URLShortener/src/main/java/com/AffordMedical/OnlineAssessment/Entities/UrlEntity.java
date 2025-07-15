package com.AffordMedical.OnlineAssessment.Entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UrlEntity {
        private String shortCode;
        private String originalUrl;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private int clickCount;
        private boolean isActive;

        public UrlEntity() {
            this.createdAt = LocalDateTime.now();
            this.clickCount = 0;
            this.isActive = true;
        }

        public UrlEntity(String shortCode, String originalUrl, int validityMinutes) {
            this();
            this.shortCode = shortCode;
            this.originalUrl = originalUrl;
            this.expiresAt = LocalDateTime.now().plusMinutes(validityMinutes);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

    }

