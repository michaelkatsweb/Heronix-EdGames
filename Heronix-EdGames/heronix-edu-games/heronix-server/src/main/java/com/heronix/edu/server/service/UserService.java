package com.heronix.edu.server.service;

import com.heronix.edu.server.entity.UserEntity;
import com.heronix.edu.server.exception.ResourceNotFoundException;
import com.heronix.edu.server.repository.UserRepository;
import com.heronix.edu.server.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for user (teacher/admin) management and authentication.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuditService auditService;

    /**
     * Authenticate a user with username and password
     */
    @Transactional
    public String authenticateUser(String username, String password) {
        logger.info("User authentication attempt: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if (!user.getActive()) {
            logger.warn("Authentication failed - user inactive: {}", username);
            throw new IllegalStateException("User account is inactive");
        }

        if (!passwordService.verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Authentication failed - invalid password: {}", username);
            auditService.logEvent(username, "LOGIN_FAILED", "User", username, "FAILURE");
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token with user ID as subject
        String token = jwtTokenProvider.generateToken(user.getUserId().toString(), user.getUsername());

        auditService.logEvent(username, "LOGIN_SUCCESS", "User", username, "SUCCESS");

        logger.info("User authenticated successfully: {}", username);

        return token;
    }

    /**
     * Create a new user
     */
    @Transactional
    public UserEntity createUser(String username, String password, String firstName,
                                  String lastName, UserEntity.UserRole role, String schoolId) {
        logger.info("Creating new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        String hashedPassword = passwordService.hashPassword(password);

        UserEntity user = new UserEntity(username, hashedPassword, firstName, lastName, role);
        user.setSchoolId(schoolId);

        UserEntity saved = userRepository.save(user);

        auditService.logEvent("SYSTEM", "USER_CREATED", "User", username, "SUCCESS");

        return saved;
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("Password change request for user ID: {}", userId);

        UserEntity user = getUserById(userId);

        if (!passwordService.verifyPassword(oldPassword, user.getPasswordHash())) {
            logger.warn("Password change failed - invalid old password: {}", userId);
            throw new IllegalArgumentException("Invalid old password");
        }

        String newHashedPassword = passwordService.hashPassword(newPassword);
        user.setPasswordHash(newHashedPassword);
        userRepository.save(user);

        auditService.logEvent(user.getUsername(), "PASSWORD_CHANGED", "User", user.getUsername(), "SUCCESS");

        logger.info("Password changed successfully for user: {}", user.getUsername());
    }
}
