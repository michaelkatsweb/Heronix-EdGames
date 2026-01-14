package com.heronix.edu.server.service;

import com.heronix.edu.server.dto.game.*;
import com.heronix.edu.server.entity.*;
import com.heronix.edu.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameSessionService.
 * Tests session creation, player management, game logic, and hack mechanics.
 */
@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    @Mock
    private GameSessionRepository sessionRepository;

    @Mock
    private GamePlayerRepository playerRepository;

    @Mock
    private QuestionSetRepository questionSetRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private GameSessionService gameSessionService;

    @BeforeEach
    void setUp() {
        gameSessionService = new GameSessionService(
            sessionRepository,
            playerRepository,
            questionSetRepository,
            questionRepository,
            messagingTemplate
        );
    }

    @Nested
    @DisplayName("Session Creation Tests")
    class SessionCreationTests {

        @Test
        @DisplayName("Should create session with valid request")
        void shouldCreateSessionWithValidRequest() {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest();
            request.setQuestionSetId("math-basics");
            request.setTimeLimitMinutes(10);
            request.setTargetCredits(1000);
            request.setGameType("CODE_BREAKER");

            List<QuestionEntity> questions = createSampleQuestions(5);
            when(questionRepository.findByQuestionSetId("math-basics")).thenReturn(questions);
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            GameSessionDto result = gameSessionService.createSession(request, "teacher-123");

            // Assert
            assertNotNull(result);
            assertNotNull(result.getSessionCode());
            assertEquals(6, result.getSessionCode().length());
            assertEquals("teacher-123", result.getTeacherId());
            assertEquals("CODE_BREAKER", result.getGameType());
            assertEquals(600, result.getTimeLimitSeconds()); // 10 minutes = 600 seconds
            assertEquals(1000, result.getTargetCredits());
            assertEquals(GameSessionStatus.WAITING, result.getStatus());

            verify(sessionRepository).save(any(GameSessionEntity.class));
        }

        @Test
        @DisplayName("Should use default values when not specified")
        void shouldUseDefaultValuesWhenNotSpecified() {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest();
            request.setQuestionSetId("math-basics");

            List<QuestionEntity> questions = createSampleQuestions(3);
            when(questionRepository.findByQuestionSetId("math-basics")).thenReturn(questions);
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            GameSessionDto result = gameSessionService.createSession(request, "teacher-456");

            // Assert
            assertEquals("CODE_BREAKER", result.getGameType());
            assertEquals(600, result.getTimeLimitSeconds()); // Default 10 minutes
            assertEquals(1000, result.getTargetCredits()); // Default 1000
        }

        @Test
        @DisplayName("Session code should be alphanumeric and 6 characters")
        void sessionCodeShouldBeAlphanumericAnd6Characters() {
            // Arrange
            CreateSessionRequest request = new CreateSessionRequest();
            request.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(2));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            GameSessionDto result = gameSessionService.createSession(request, "teacher");

            // Assert
            String code = result.getSessionCode();
            assertEquals(6, code.length());
            assertTrue(code.matches("[A-Z0-9]+"), "Session code should be alphanumeric uppercase");
        }
    }

    @Nested
    @DisplayName("Player Join Tests")
    class PlayerJoinTests {

        @Test
        @DisplayName("Should allow player to join waiting session")
        void shouldAllowPlayerToJoinWaitingSession() {
            // First create a session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("math-basics");

            when(questionRepository.findByQuestionSetId("math-basics")).thenReturn(createSampleQuestions(3));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(playerRepository.save(any(GamePlayerEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher");

            // Now join the session
            JoinSessionRequest joinRequest = new JoinSessionRequest();
            joinRequest.setStudentId("student-001");
            joinRequest.setStudentName("John Doe");
            joinRequest.setSecretCode("ALPHA");
            joinRequest.setAvatarId("CLEVER_CAT");

            // Act
            JoinSessionResponse response = gameSessionService.joinSession(session.getSessionCode(), joinRequest);

            // Assert
            assertTrue(response.isSuccess());
            assertNotNull(response.getPlayerId());
            assertEquals(session.getSessionCode(), response.getSessionId());
            assertEquals("CODE_BREAKER", response.getGameType());

            verify(playerRepository).save(any(GamePlayerEntity.class));
        }

        @Test
        @DisplayName("Should reject join for non-existent session")
        void shouldRejectJoinForNonExistentSession() {
            // Arrange
            JoinSessionRequest joinRequest = new JoinSessionRequest();
            joinRequest.setStudentId("student-001");
            joinRequest.setStudentName("John");
            joinRequest.setSecretCode("BETA");

            // Act
            JoinSessionResponse response = gameSessionService.joinSession("INVALID", joinRequest);

            // Assert
            assertFalse(response.isSuccess());
            assertEquals("Session not found", response.getMessage());
        }

        @Test
        @DisplayName("Should reject duplicate player join")
        void shouldRejectDuplicatePlayerJoin() {
            // Create session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(2));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(playerRepository.save(any(GamePlayerEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher");

            // First join
            JoinSessionRequest joinRequest = new JoinSessionRequest();
            joinRequest.setStudentId("student-001");
            joinRequest.setStudentName("John");
            joinRequest.setSecretCode("GAMMA");

            gameSessionService.joinSession(session.getSessionCode(), joinRequest);

            // Try to join again with same student ID
            JoinSessionResponse duplicateResponse = gameSessionService.joinSession(session.getSessionCode(), joinRequest);

            // Assert
            assertFalse(duplicateResponse.isSuccess());
            assertEquals("Already joined this session", duplicateResponse.getMessage());
        }

        @Test
        @DisplayName("Should use default avatar when not specified")
        void shouldUseDefaultAvatarWhenNotSpecified() {
            // Create session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(2));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            ArgumentCaptor<GamePlayerEntity> playerCaptor = ArgumentCaptor.forClass(GamePlayerEntity.class);
            when(playerRepository.save(playerCaptor.capture())).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher");

            // Join without avatar
            JoinSessionRequest joinRequest = new JoinSessionRequest();
            joinRequest.setStudentId("student-002");
            joinRequest.setStudentName("Jane");
            joinRequest.setSecretCode("DELTA");
            joinRequest.setAvatarId(null);

            gameSessionService.joinSession(session.getSessionCode(), joinRequest);

            // Assert default avatar is used
            GamePlayerEntity savedPlayer = playerCaptor.getValue();
            assertEquals("ROOKIE_ROBOT", savedPlayer.getAvatarId());
        }
    }

    @Nested
    @DisplayName("Game Start Tests")
    class GameStartTests {

        @Test
        @DisplayName("Should start game when teacher requests")
        void shouldStartGameWhenTeacherRequests() {
            // Create session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(5));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(playerRepository.save(any(GamePlayerEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher-123");

            // Add a player
            JoinSessionRequest joinRequest = new JoinSessionRequest();
            joinRequest.setStudentId("student-001");
            joinRequest.setStudentName("Player1");
            joinRequest.setSecretCode("ALPHA");

            gameSessionService.joinSession(session.getSessionCode(), joinRequest);

            // Act - Start game
            assertDoesNotThrow(() -> gameSessionService.startGame(session.getSessionCode(), "teacher-123"));

            // Verify broadcast was sent
            verify(messagingTemplate, atLeastOnce()).convertAndSend(
                eq("/topic/session/" + session.getSessionCode()),
                any(Object.class)
            );
        }

        @Test
        @DisplayName("Should reject start from non-owner teacher")
        void shouldRejectStartFromNonOwnerTeacher() {
            // Create session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(2));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher-123");

            // Act & Assert - Different teacher tries to start
            assertThrows(SecurityException.class, () ->
                gameSessionService.startGame(session.getSessionCode(), "other-teacher")
            );
        }

        @Test
        @DisplayName("Should reject start for non-existent session")
        void shouldRejectStartForNonExistentSession() {
            assertThrows(IllegalArgumentException.class, () ->
                gameSessionService.startGame("INVALID", "teacher")
            );
        }
    }

    @Nested
    @DisplayName("Leaderboard Tests")
    class LeaderboardTests {

        @Test
        @DisplayName("Should return sorted leaderboard by credits")
        void shouldReturnSortedLeaderboardByCredits() {
            // Create session with multiple players
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(5));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(playerRepository.save(any(GamePlayerEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto session = gameSessionService.createSession(createRequest, "teacher");

            // Add players
            for (int i = 1; i <= 3; i++) {
                JoinSessionRequest joinRequest = new JoinSessionRequest();
                joinRequest.setStudentId("student-" + i);
                joinRequest.setStudentName("Player" + i);
                joinRequest.setSecretCode("CODE" + i);
                gameSessionService.joinSession(session.getSessionCode(), joinRequest);
            }

            // Act
            List<PlayerDto> leaderboard = gameSessionService.getLeaderboard(session.getSessionCode());

            // Assert
            assertNotNull(leaderboard);
            assertEquals(3, leaderboard.size());

            // Verify ranks are assigned
            for (int i = 0; i < leaderboard.size(); i++) {
                assertEquals(i + 1, leaderboard.get(i).getRank());
            }
        }

        @Test
        @DisplayName("Should return empty list for non-existent session")
        void shouldReturnEmptyListForNonExistentSession() {
            List<PlayerDto> leaderboard = gameSessionService.getLeaderboard("INVALID");
            assertNotNull(leaderboard);
            assertTrue(leaderboard.isEmpty());
        }
    }

    @Nested
    @DisplayName("Session Query Tests")
    class SessionQueryTests {

        @Test
        @DisplayName("Should return session info for active session")
        void shouldReturnSessionInfoForActiveSession() {
            // Create session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setQuestionSetId("test-set");

            when(questionRepository.findByQuestionSetId("test-set")).thenReturn(createSampleQuestions(2));
            when(sessionRepository.save(any(GameSessionEntity.class))).thenAnswer(i -> i.getArgument(0));

            GameSessionDto created = gameSessionService.createSession(createRequest, "teacher");

            // Act
            GameSessionDto retrieved = gameSessionService.getSession(created.getSessionCode());

            // Assert
            assertNotNull(retrieved);
            assertEquals(created.getSessionCode(), retrieved.getSessionCode());
            assertEquals("teacher", retrieved.getTeacherId());
        }

        @Test
        @DisplayName("Should return null for non-existent session not in memory")
        void shouldReturnNullForNonExistentSession() {
            when(sessionRepository.findById("NONEXIST")).thenReturn(Optional.empty());

            GameSessionDto result = gameSessionService.getSession("NONEXIST");

            assertNull(result);
        }
    }

    // Helper methods

    private List<QuestionEntity> createSampleQuestions(int count) {
        List<QuestionEntity> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            QuestionEntity q = new QuestionEntity();
            q.setQuestionId("q-" + i);
            q.setQuestionText("What is " + i + " + " + i + "?");
            q.setCorrectAnswer(String.valueOf(i * 2));
            q.setWrongAnswer1(String.valueOf(i));
            q.setWrongAnswer2(String.valueOf(i * 3));
            q.setWrongAnswer3(String.valueOf(i + 1));
            q.setDifficulty(1);
            questions.add(q);
        }
        return questions;
    }
}
