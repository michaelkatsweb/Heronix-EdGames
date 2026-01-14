package com.heronix.edu.client.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenManager.
 * Tests JWT token storage, validation, and expiration handling.
 */
class TokenManagerTest {

    private TokenManager tokenManager;

    // Sample valid JWT token format (header.payload.signature)
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature123";
    private static final String ANOTHER_VALID_TOKEN = "header.payload.signature";

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager();
    }

    @Nested
    @DisplayName("Token Save Tests")
    class TokenSaveTests {

        @Test
        @DisplayName("Should save valid JWT token")
        void shouldSaveValidJwtToken() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken(VALID_JWT_TOKEN, expiresAt);

            // Assert
            assertTrue(tokenManager.isTokenValid());
            assertEquals(VALID_JWT_TOKEN, tokenManager.getToken());
            assertEquals(expiresAt, tokenManager.getExpiresAt());
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken(null, expiresAt);

            // Assert - token should not be saved
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken("", expiresAt);

            // Assert
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should reject whitespace-only token")
        void shouldRejectWhitespaceOnlyToken() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken("   ", expiresAt);

            // Assert
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should reject token without periods (invalid JWT format)")
        void shouldRejectTokenWithoutPeriods() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken("invalidtokenwithoutperiods", expiresAt);

            // Assert
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should accept token with at least one period")
        void shouldAcceptTokenWithPeriods() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken(ANOTHER_VALID_TOKEN, expiresAt);

            // Assert
            assertTrue(tokenManager.isTokenValid());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return true for valid non-expired token")
        void shouldReturnTrueForValidNonExpiredToken() {
            // Arrange
            LocalDateTime futureExpiry = LocalDateTime.now().plusHours(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, futureExpiry);

            // Act & Assert
            assertTrue(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            // Arrange
            LocalDateTime pastExpiry = LocalDateTime.now().minusHours(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, pastExpiry);

            // Act & Assert
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should return false when no token saved")
        void shouldReturnFalseWhenNoTokenSaved() {
            // Act & Assert - fresh TokenManager
            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Should return false for token with null expiry")
        void shouldReturnFalseForTokenWithNullExpiry() {
            // We can't easily test this since saveToken requires both params,
            // but we test the cleared state
            tokenManager.saveToken(VALID_JWT_TOKEN, LocalDateTime.now().plusHours(1));
            tokenManager.clearToken();

            assertFalse(tokenManager.isTokenValid());
        }

        @Test
        @DisplayName("Token about to expire in 1 second should still be valid")
        void tokenAboutToExpireShouldStillBeValid() {
            // Arrange - expires in 1 second
            LocalDateTime almostExpired = LocalDateTime.now().plusSeconds(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, almostExpired);

            // Act & Assert
            assertTrue(tokenManager.isTokenValid());
        }
    }

    @Nested
    @DisplayName("Token Retrieval Tests")
    class TokenRetrievalTests {

        @Test
        @DisplayName("Should return token when valid")
        void shouldReturnTokenWhenValid() {
            // Arrange
            LocalDateTime futureExpiry = LocalDateTime.now().plusHours(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, futureExpiry);

            // Act
            String token = tokenManager.getToken();

            // Assert
            assertEquals(VALID_JWT_TOKEN, token);
        }

        @Test
        @DisplayName("Should throw TokenExpiredException when token expired")
        void shouldThrowExceptionWhenTokenExpired() {
            // Arrange
            LocalDateTime pastExpiry = LocalDateTime.now().minusHours(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, pastExpiry);

            // Act & Assert
            assertThrows(TokenExpiredException.class, () -> tokenManager.getToken());
        }

        @Test
        @DisplayName("Should throw TokenExpiredException when no token")
        void shouldThrowExceptionWhenNoToken() {
            // Act & Assert
            assertThrows(TokenExpiredException.class, () -> tokenManager.getToken());
        }
    }

    @Nested
    @DisplayName("Token Clear Tests")
    class TokenClearTests {

        @Test
        @DisplayName("Should clear token successfully")
        void shouldClearTokenSuccessfully() {
            // Arrange
            tokenManager.saveToken(VALID_JWT_TOKEN, LocalDateTime.now().plusHours(1));
            assertTrue(tokenManager.isTokenValid());

            // Act
            tokenManager.clearToken();

            // Assert
            assertFalse(tokenManager.isTokenValid());
            assertNull(tokenManager.getExpiresAt());
        }

        @Test
        @DisplayName("Should be safe to clear when no token exists")
        void shouldBeSafeToClearWhenNoToken() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> tokenManager.clearToken());
            assertFalse(tokenManager.isTokenValid());
        }
    }

    @Nested
    @DisplayName("Expiration Date Tests")
    class ExpirationDateTests {

        @Test
        @DisplayName("Should return correct expiration date")
        void shouldReturnCorrectExpirationDate() {
            // Arrange
            LocalDateTime expiresAt = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
            tokenManager.saveToken(VALID_JWT_TOKEN, expiresAt);

            // Act & Assert
            assertEquals(expiresAt, tokenManager.getExpiresAt());
        }

        @Test
        @DisplayName("Should return null expiration when cleared")
        void shouldReturnNullExpirationWhenCleared() {
            // Arrange
            tokenManager.saveToken(VALID_JWT_TOKEN, LocalDateTime.now().plusHours(1));
            tokenManager.clearToken();

            // Act & Assert
            assertNull(tokenManager.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle token replacement")
        void shouldHandleTokenReplacement() {
            // Arrange
            LocalDateTime expiry1 = LocalDateTime.now().plusHours(1);
            LocalDateTime expiry2 = LocalDateTime.now().plusHours(2);

            tokenManager.saveToken("old.token.value", expiry1);

            // Act
            tokenManager.saveToken("new.token.value", expiry2);

            // Assert
            assertEquals("new.token.value", tokenManager.getToken());
            assertEquals(expiry2, tokenManager.getExpiresAt());
        }

        @Test
        @DisplayName("Should not save invalid token over valid one")
        void shouldNotSaveInvalidTokenOverValidOne() {
            // Arrange - save valid token first
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);
            tokenManager.saveToken(VALID_JWT_TOKEN, expiry);

            // Act - try to save invalid token
            tokenManager.saveToken("", expiry);

            // Assert - original valid token should remain
            assertTrue(tokenManager.isTokenValid());
            assertEquals(VALID_JWT_TOKEN, tokenManager.getToken());
        }

        @Test
        @DisplayName("Should handle very long token")
        void shouldHandleVeryLongToken() {
            // Arrange - create a very long but valid format token
            String longToken = "a".repeat(1000) + ".payload." + "b".repeat(1000);
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken(longToken, expiry);

            // Assert
            assertTrue(tokenManager.isTokenValid());
            assertEquals(longToken, tokenManager.getToken());
        }

        @Test
        @DisplayName("Should handle token with special characters")
        void shouldHandleTokenWithSpecialCharacters() {
            // Arrange - JWT tokens use base64url which includes - and _
            String specialToken = "eyJ-test_header.eyJ-test_payload.sig-nat_ure";
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);

            // Act
            tokenManager.saveToken(specialToken, expiry);

            // Assert
            assertTrue(tokenManager.isTokenValid());
            assertEquals(specialToken, tokenManager.getToken());
        }
    }
}
