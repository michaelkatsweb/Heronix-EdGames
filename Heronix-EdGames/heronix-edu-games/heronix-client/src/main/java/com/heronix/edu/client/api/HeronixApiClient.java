package com.heronix.edu.client.api;

import com.heronix.edu.client.api.dto.*;
import com.heronix.edu.client.api.exception.ApiException;
import com.heronix.edu.client.api.exception.NetworkException;
import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.config.HttpClientConfig;
import com.heronix.edu.client.security.TokenExpiredException;
import com.heronix.edu.client.security.TokenManager;
import com.heronix.edu.client.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

/**
 * REST API client for communicating with the Heronix server
 */
public class HeronixApiClient {
    private static final Logger logger = LoggerFactory.getLogger(HeronixApiClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final TokenManager tokenManager;

    public HeronixApiClient(String baseUrl, TokenManager tokenManager) {
        this.httpClient = HttpClientConfig.getHttpClient();
        this.baseUrl = baseUrl;
        this.tokenManager = tokenManager;
    }

    public HeronixApiClient(TokenManager tokenManager) {
        this(AppConfig.getServerUrl(), tokenManager);
    }

    /**
     * Register a new device with the server
     */
    public DeviceRegistrationResponse registerDevice(DeviceRegistrationRequest request) {
        logger.info("Registering device: {}", request.getDeviceId());

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/device/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(request)))
            .build();

        return sendRequest(httpRequest, DeviceRegistrationResponse.class);
    }

    /**
     * Check device approval status
     */
    public DeviceStatusResponse getDeviceStatus(String deviceId) {
        logger.debug("Checking device status: {}", deviceId);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/device/status?deviceId=" + deviceId))
            .GET()
            .build();

        return sendRequest(httpRequest, DeviceStatusResponse.class);
    }

    /**
     * Authenticate device and get JWT token
     */
    public AuthResponse authenticateDevice(String deviceId, String studentId) {
        logger.info("Authenticating device: {}", deviceId);

        DeviceAuthRequest request = new DeviceAuthRequest(deviceId, studentId);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/auth/device"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(request)))
            .build();

        return sendRequest(httpRequest, AuthResponse.class);
    }

    /**
     * Upload game scores to server
     */
    public SyncResponse uploadScores(String deviceId, List<GameScoreDto> scores) {
        logger.info("Uploading {} scores to server", scores.size());

        SyncUploadRequest request = new SyncUploadRequest(deviceId, scores);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/sync/upload"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + tokenManager.getToken())
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(request)))
            .build();

        return sendRequest(httpRequest, SyncResponse.class);
    }

    /**
     * Ping server to test connectivity
     */
    public void ping() {
        logger.debug("Pinging server");

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/ping"))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("Ping failed: HTTP " + response.statusCode(), response.statusCode());
            }
        } catch (IOException e) {
            throw new NetworkException("Failed to ping server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Ping interrupted", e);
        }
    }

    /**
     * Send HTTP request and parse response
     */
    private <T> T sendRequest(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            logger.debug("HTTP {} {} -> {}", request.method(), request.uri(), response.statusCode());

            // Handle authentication errors
            if (response.statusCode() == 401) {
                throw new TokenExpiredException("Token expired or invalid");
            }

            // Handle client errors
            if (response.statusCode() >= 400 && response.statusCode() < 500) {
                throw new ApiException("HTTP " + response.statusCode() + ": " + response.body(),
                                     response.statusCode());
            }

            // Handle server errors
            if (response.statusCode() >= 500) {
                throw new ApiException("Server error: HTTP " + response.statusCode(),
                                     response.statusCode());
            }

            // Parse successful response
            return JsonUtil.fromJson(response.body(), responseType);

        } catch (IOException e) {
            logger.error("Network error communicating with server", e);
            throw new NetworkException("Failed to connect to server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Request interrupted", e);
        }
    }
}
