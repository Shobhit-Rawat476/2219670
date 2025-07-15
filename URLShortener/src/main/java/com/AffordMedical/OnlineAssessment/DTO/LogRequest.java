package com.AffordMedical.OnlineAssessment.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogRequest {
    private String stack;
    private String level;
    private String packageName;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public LogRequest(String stack, String level, String packageName, String message) {
        this.stack = stack;
        this.level = level;
        this.packageName = packageName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
