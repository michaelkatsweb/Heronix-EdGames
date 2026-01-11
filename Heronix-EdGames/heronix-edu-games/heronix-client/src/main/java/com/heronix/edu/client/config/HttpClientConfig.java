package com.heronix.edu.client.config;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for HTTP client used to communicate with the server
 */
public class HttpClientConfig {

    private static HttpClient httpClient;

    /**
     * Get or create the HTTP client instance
     */
    public static synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        }
        return httpClient;
    }

    /**
     * Close the HTTP client (if needed for cleanup)
     */
    public static synchronized void shutdown() {
        if (httpClient != null) {
            // HttpClient doesn't require explicit shutdown
            httpClient = null;
        }
    }
}
