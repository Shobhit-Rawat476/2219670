package com.AffordMedical.OnlineAssessment.Services;
import com.AffordMedical.OnlineAssessment.DTO.LogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
public class LoggingService {
    private static final Logger logger= LoggerFactory.getLogger(LoggingService.class);

    @Value("${logging.api.endpoint:http://20.244.56.144/evaluation-service/logs}")
    private String apiEndpoint;

    @Autowired
    private final RestTemplate restTemplate;
    public LoggingService(){
        this.restTemplate=new RestTemplate();
    }
    public void log(String stack, String level, String packageName, String message) {
        try {
            // Create log request
            LogRequest logRequest = new LogRequest(stack, level, packageName, message);

            // Send to API
            sendLogToApi(logRequest);

            logger.info("Log sent to API successfully");

        } catch (Exception e) {
            logger.error("Failed to send log to API: " + e.getMessage(), e);
        }
    }

    private void sendLogToApi(LogRequest logRequest) {
        String token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiYXVkIjoiaHR0cDovLzIwLjI0NC41Ni4xNDQvZXZhbHVhdGlvbi1zZXJ2aWNlIiwiZW1haWwiOiJzaG9iaGl0cm9tZW5lQGdtYWlsLmNvbSIsImV4cCI6MTc1MjU1NzU2MCwiaWF0IjoxNzUyNTU2NjYwLCJpc3MiOiJBZmZvcmQgTWVkaWNhbCBUZWNobm9sb2dpZXMgUHJpdmF0ZSBMaW1pdGVkIiwianRpIjoiMDM1OGQ2ZjctODJjZS00MjFmLWFkN2UtYzdkMGZmYmQ0OGVhIiwibG9jYWxlIjoiZW4tSU4iLCJuYW1lIjoic2hvYmhpdCByYXdhdCIsInN1YiI6Ijc2ZjA4NmQ5LTcyODAtNDZhYS1iMjgxLWE0NzBkNTkyNWE5NyJ9LCJlbWFpbCI6InNob2JoaXRyb21lbmVAZ21haWwuY29tIiwibmFtZSI6InNob2JoaXQgcmF3YXQiLCJyb2xsTm8iOiIyMjE5NjcwIiwiYWNjZXNzQ29kZSI6IlFBaERVciIsImNsaWVudElEIjoiNzZmMDg2ZDktNzI4MC00NmFhLWIyODEtYTQ3MGQ1OTI1YTk3IiwiY2xpZW50U2VjcmV0IjoiV3RGU0RQZEJBc3lDSlJTaiJ9.EUlpCTuxRWEim-HwJOh5p-jABKpcthmX40Bf4YVcfuw";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<LogRequest> entity = new HttpEntity<>(logRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiEndpoint,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }

}
