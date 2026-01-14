package com.heronix.edu.server.config;

import com.heronix.edu.server.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration.
 * Configures JWT-based authentication and authorization for the server.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST API
                .csrf(csrf -> csrf.disable())

                // Configure CORS for local network access
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/ping",
                                "/api/device/register",
                                "/api/device/status",
                                "/api/auth/device",
                                "/api/teacher/login",
                                "/api/device/management/**",  // Temporarily public for testing
                                "/api/games/list",  // Public game catalog
                                "/api/games/*/info",  // Public game info
                                "/api/games/*/icon",  // Public game icons
                                "/api/games/*/screenshot/*",  // Public game screenshots
                                "/api/games/*/metadata",  // Public game metadata
                                "/api/games/metadata/all",  // Public all metadata
                                "/api/bundles",  // Public bundle list
                                "/api/bundles/standard",  // Public standard bundles
                                "/api/bundles/premium",  // Public premium bundles
                                "/api/bundles/*",  // Public bundle details
                                "/api/bundles/access/**",  // Public access checks
                                "/ws/**",  // WebSocket endpoints for multiplayer games
                                "/api/game/sessions",  // Public session creation (for Teacher Portal)
                                "/api/game/sessions/**",  // All session endpoints public (protected by session codes)
                                "/api/game/question-sets",  // Public question sets
                                "/api/game/question-sets/**",  // Public question set operations
                                "/api/monitor/**",  // WebSocket monitoring endpoints
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/error"
                        ).permitAll()

                        // Protected endpoints - authentication required
                        .requestMatchers("/api/sync/**").authenticated()
                        .requestMatchers("/api/games/*/download").authenticated()  // Downloading requires auth
                        .requestMatchers("/api/teacher/**").authenticated()
                        .requestMatchers("/api/reports/**").authenticated()
                        .requestMatchers("/api/analytics/**").authenticated()  // Play time analytics
                        .requestMatchers("/api/game/sessions").authenticated()  // Creating sessions requires auth
                        .requestMatchers("/api/game/sessions/*/start").authenticated()
                        .requestMatchers("/api/game/sessions/*/end").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console frames (for development)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * CORS configuration for local network access
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from local network
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
