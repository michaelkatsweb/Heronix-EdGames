package com.heronix.edu.server.config;

import com.heronix.edu.server.websocket.GameSessionChannelInterceptor;
import com.heronix.edu.server.websocket.GameSessionHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * High-performance WebSocket configuration for multiplayer game support.
 * Optimized to handle 500+ concurrent student connections.
 *
 * Key optimizations:
 * - Dedicated thread pools for inbound/outbound channels (20-100 threads)
 * - Increased buffer sizes and message limits
 * - Session-code based authentication (no JWT for WebSocket)
 * - Heartbeat configuration for connection health
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private GameSessionHandshakeInterceptor handshakeInterceptor;

    @Autowired
    private GameSessionChannelInterceptor channelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker with heartbeat for connection health monitoring
        // Heartbeat: server sends every 10s, expects client response every 10s
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{10000, 10000})
            .setTaskScheduler(brokerHeartbeatScheduler());

        // Prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");

        // Preserve publish order for consistent game state
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WebSocket endpoint with session-based auth
        registry.addEndpoint("/ws/game")
            .addInterceptors(handshakeInterceptor)
            .setAllowedOriginPatterns("*");

        // SockJS fallback endpoint for browsers without WebSocket support
        registry.addEndpoint("/ws/game")
            .addInterceptors(handshakeInterceptor)
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setHeartbeatTime(25000)  // 25 second heartbeat
            .setDisconnectDelay(5000)  // 5 second disconnect delay
            .setStreamBytesLimit(512 * 1024)  // 512KB stream limit
            .setHttpMessageCacheSize(1000);  // Cache 1000 messages for SockJS
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Optimize for high concurrency game messaging
        registration
            .setMessageSizeLimit(128 * 1024)      // 128KB max message size
            .setSendBufferSizeLimit(1024 * 1024)  // 1MB send buffer
            .setSendTimeLimit(30 * 1000)          // 30 second send timeout
            .setTimeToFirstMessage(60 * 1000);    // 60 second initial timeout
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Configure inbound channel for handling client messages
        // Optimized for 500+ concurrent connections
        // Core: 20 threads, Max: 100 threads (handles bursts during game events)
        registration.taskExecutor()
            .corePoolSize(20)
            .maxPoolSize(100)
            .queueCapacity(500)
            .keepAliveSeconds(60);
        registration.interceptors(channelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure outbound channel for sending messages to clients
        // Larger pool for broadcasting game events
        // Core: 30 threads, Max: 150 threads
        registration.taskExecutor()
            .corePoolSize(30)
            .maxPoolSize(150)
            .queueCapacity(1000)
            .keepAliveSeconds(60);
    }

    /**
     * Scheduler for broker heartbeat tasks.
     * Named differently to avoid bean conflicts.
     */
    @Bean
    public ThreadPoolTaskScheduler brokerHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Configure the underlying WebSocket server container.
     * Tomcat/Jetty specific settings for high concurrency.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Maximum message size (128KB)
        container.setMaxTextMessageBufferSize(128 * 1024);
        container.setMaxBinaryMessageBufferSize(128 * 1024);
        // Maximum session idle timeout (10 minutes)
        container.setMaxSessionIdleTimeout(600000L);
        // Async send timeout (30 seconds)
        container.setAsyncSendTimeout(30000L);
        return container;
    }
}
