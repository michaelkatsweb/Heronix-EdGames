package com.heronix.edu.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * API Client for Heronix SIS (Student Information System)
 * Fetches student data from the main SIS server
 */
@Service
public class SisApiClient {

    private static final Logger log = LoggerFactory.getLogger(SisApiClient.class);

    @Value("${heronix.sis.url:http://localhost:9580}")
    private String sisBaseUrl;

    @Value("${heronix.sis.enabled:true}")
    private boolean sisEnabled;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String TEST_STUDENT_ID = "testgm1";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SisApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    /**
     * Validate if a student ID exists in the SIS system
     * Special case: "testgm1" is always valid for testing
     *
     * @param studentId The student ID to validate
     * @return true if the student exists, false otherwise
     */
    public boolean validateStudentId(String studentId) {
        // Special test student ID - always valid
        if (TEST_STUDENT_ID.equalsIgnoreCase(studentId)) {
            log.info("Test student ID detected: {}", studentId);
            return true;
        }

        // If SIS integration is disabled, accept all student IDs
        if (!sisEnabled) {
            log.debug("SIS integration disabled - accepting student ID: {}", studentId);
            return true;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sisBaseUrl + "/api/students/" + studentId))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Student ID validated successfully from SIS: {}", studentId);
                return true;
            } else if (response.statusCode() == 404) {
                log.warn("Student ID not found in SIS: {}", studentId);
                return false;
            } else {
                log.error("Unexpected response from SIS for student {}: {}", studentId, response.statusCode());
                // On error, accept the student ID (fail open for availability)
                return true;
            }

        } catch (Exception e) {
            log.error("Error connecting to SIS server for student validation: {}", studentId, e);
            // On error, accept the student ID (fail open for availability)
            return true;
        }
    }

    /**
     * Get student information from SIS
     * Special case: Returns mock data for "testgm1"
     *
     * @param studentId The student ID
     * @return Student information map, or null if not found
     */
    public Map<String, Object> getStudentInfo(String studentId) {
        // Special test student - return mock data
        if (TEST_STUDENT_ID.equalsIgnoreCase(studentId)) {
            log.info("Returning mock data for test student: {}", studentId);
            return Map.of(
                    "studentId", TEST_STUDENT_ID,
                    "firstName", "Test",
                    "lastName", "Student",
                    "grade", "9",
                    "active", true
            );
        }

        if (!sisEnabled) {
            log.debug("SIS integration disabled - returning minimal student info");
            return Map.of(
                    "studentId", studentId,
                    "firstName", "Unknown",
                    "lastName", "Student",
                    "active", true
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sisBaseUrl + "/api/students/" + studentId))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> studentData = objectMapper.readValue(response.body(), Map.class);
                log.info("Student info retrieved from SIS: {}", studentId);
                return studentData;
            } else {
                log.warn("Student not found in SIS: {}", studentId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error fetching student info from SIS: {}", studentId, e);
            return null;
        }
    }

    /**
     * Check if SIS server is reachable
     *
     * @return true if SIS is available, false otherwise
     */
    public boolean isSisAvailable() {
        if (!sisEnabled) {
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sisBaseUrl + "/actuator/health"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            log.debug("SIS server not reachable", e);
            return false;
        }
    }
}
