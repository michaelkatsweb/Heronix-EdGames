package com.heronix.edu.server.service;

import com.heronix.edu.server.dto.game.*;
import com.heronix.edu.server.entity.*;
import com.heronix.edu.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing multiplayer game sessions.
 * Handles session creation, player management, game logic, and real-time updates.
 */
@Service
public class GameSessionService {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);

    private final GameSessionRepository sessionRepository;
    private final GamePlayerRepository playerRepository;
    private final QuestionSetRepository questionSetRepository;
    private final QuestionRepository questionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // In-memory active session state for performance
    private final Map<String, ActiveGameSession> activeSessions = new ConcurrentHashMap<>();

    // Track failed hack attempts: sessionId -> (hackerId -> (targetId -> failCount))
    private final Map<String, Map<String, Map<String, Integer>>> hackAttemptTracker = new ConcurrentHashMap<>();

    private static final String SESSION_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SESSION_CODE_LENGTH = 6;
    private static final int BASE_CREDITS_PER_CORRECT = 50;
    private static final double HACK_STEAL_PERCENTAGE = 0.25;  // Steal 25% of target's credits

    public GameSessionService(GameSessionRepository sessionRepository,
                               GamePlayerRepository playerRepository,
                               QuestionSetRepository questionSetRepository,
                               QuestionRepository questionRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
        this.questionSetRepository = questionSetRepository;
        this.questionRepository = questionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create a new game session.
     */
    @Transactional
    public GameSessionDto createSession(CreateSessionRequest request, String teacherId) {
        String sessionCode = generateSessionCode();

        GameSessionEntity session = new GameSessionEntity();
        session.setSessionId(sessionCode);
        session.setTeacherId(teacherId);
        session.setGameType(request.getGameType() != null ? request.getGameType() : "CODE_BREAKER");
        session.setQuestionSetId(request.getQuestionSetId());
        session.setTimeLimitSeconds(request.getTimeLimitMinutes() != null ? request.getTimeLimitMinutes() * 60 : 600);
        session.setTargetCredits(request.getTargetCredits() != null ? request.getTargetCredits() : 1000);
        session.setStatus(GameSessionStatus.WAITING);
        session.setCreatedAt(LocalDateTime.now());

        sessionRepository.save(session);

        // Load questions into memory
        List<QuestionEntity> questions = questionRepository.findByQuestionSetId(request.getQuestionSetId());
        ActiveGameSession activeSession = new ActiveGameSession(session, questions);
        activeSessions.put(sessionCode, activeSession);

        logger.info("Created game session {} for teacher {}", sessionCode, teacherId);

        return toDto(session);
    }

    /**
     * Join an existing session.
     */
    @Transactional
    public JoinSessionResponse joinSession(String sessionCode, JoinSessionRequest request) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) {
            return JoinSessionResponse.error("Session not found");
        }

        if (activeSession.getStatus() != GameSessionStatus.WAITING) {
            return JoinSessionResponse.error("Game has already started");
        }

        // Check if player already in session
        if (activeSession.hasPlayer(request.getStudentId())) {
            return JoinSessionResponse.error("Already joined this session");
        }

        // Create player
        String playerId = UUID.randomUUID().toString();
        GamePlayerEntity player = new GamePlayerEntity(playerId, request.getStudentId(), request.getStudentName());
        player.setSecretCode(request.getSecretCode());
        player.setAvatarId(request.getAvatarId() != null ? request.getAvatarId() : "ROOKIE_ROBOT");
        player.setSession(activeSession.getSessionEntity());

        playerRepository.save(player);
        activeSession.addPlayer(player);

        // Notify other players and teacher
        GameEvent joinEvent = GameEvent.playerJoined(sessionCode, playerId, request.getStudentName());
        broadcastToSession(sessionCode, joinEvent);
        notifyTeacher(sessionCode, joinEvent);

        // Get list of other players
        List<PlayerDto> otherPlayers = activeSession.getPlayers().stream()
            .filter(p -> !p.getPlayerId().equals(playerId))
            .map(this::toPlayerDto)
            .toList();

        logger.info("Player {} joined session {}", request.getStudentName(), sessionCode);

        return JoinSessionResponse.success(playerId, sessionCode, activeSession.getGameType(), otherPlayers);
    }

    /**
     * Start the game.
     */
    @Transactional
    public void startGame(String sessionCode, String teacherId) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) {
            throw new IllegalArgumentException("Session not found");
        }

        if (!activeSession.getTeacherId().equals(teacherId)) {
            throw new SecurityException("Only the session creator can start the game");
        }

        if (activeSession.getStatus() != GameSessionStatus.WAITING) {
            throw new IllegalStateException("Game has already started");
        }

        activeSession.setStatus(GameSessionStatus.ACTIVE);
        activeSession.setStartedAt(LocalDateTime.now());

        // Update database
        GameSessionEntity entity = activeSession.getSessionEntity();
        entity.setStatus(GameSessionStatus.ACTIVE);
        entity.setStartedAt(LocalDateTime.now());
        sessionRepository.save(entity);

        // Broadcast game start
        GameEvent startEvent = GameEvent.gameStarted(sessionCode, activeSession.getTimeLimitSeconds());
        broadcastToSession(sessionCode, startEvent);

        // Send first question to each player
        for (GamePlayerEntity player : activeSession.getPlayers()) {
            sendNextQuestion(sessionCode, player.getPlayerId());
        }

        logger.info("Game started for session {}", sessionCode);
    }

    /**
     * Process an answer submission.
     */
    @Transactional
    public AnswerResult processAnswer(String sessionCode, String playerId, AnswerRequest request) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null || activeSession.getStatus() != GameSessionStatus.ACTIVE) {
            return AnswerResult.incorrect("Session not active", null);
        }

        GamePlayerEntity player = activeSession.getPlayer(playerId);
        if (player == null) {
            return AnswerResult.incorrect("Player not found", null);
        }

        QuestionEntity question = activeSession.getCurrentQuestion(playerId);
        if (question == null || !question.getQuestionId().equals(request.getQuestionId())) {
            return AnswerResult.incorrect("Invalid question", null);
        }

        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(request.getAnswer().trim());

        if (isCorrect) {
            player.incrementCorrectAnswers();
            playerRepository.save(player);

            // Create reward options
            List<RewardOption> options = Arrays.asList(
                RewardOption.earnCredits(BASE_CREDITS_PER_CORRECT),
                RewardOption.hackAttempt(),
                RewardOption.shield(30)
            );

            // Notify teacher
            notifyTeacherOfAnswer(sessionCode, player, true);

            return AnswerResult.correct(options);
        } else {
            player.incrementIncorrectAnswers();
            playerRepository.save(player);

            // Notify teacher
            notifyTeacherOfAnswer(sessionCode, player, false);

            // Move to next question
            sendNextQuestion(sessionCode, playerId);

            return AnswerResult.incorrect(question.getCorrectAnswer(), question.getExplanation());
        }
    }

    /**
     * Process reward selection after correct answer.
     */
    @Transactional
    public void processRewardSelection(String sessionCode, String playerId, String rewardType) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) return;

        GamePlayerEntity player = activeSession.getPlayer(playerId);
        if (player == null) return;

        switch (rewardType) {
            case "CREDITS" -> {
                int credits = getAvatarCreditBonus(player.getAvatarId());
                player.addCredits(BASE_CREDITS_PER_CORRECT + credits);
                playerRepository.save(player);
                sendNextQuestion(sessionCode, playerId);
            }
            case "SHIELD" -> {
                activeSession.setPlayerShield(playerId, 30);  // 30 second shield
                sendNextQuestion(sessionCode, playerId);
            }
            case "HACK" -> {
                // Player will choose target via HackRequest
                // Don't send next question yet
            }
        }

        // Update leaderboard
        broadcastLeaderboard(sessionCode);
    }

    /**
     * Process a hack attempt.
     */
    @Transactional
    public HackResult processHack(String sessionCode, String hackerId, HackRequest request) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) {
            return HackResult.failure("Session not found", 0);
        }

        GamePlayerEntity hacker = activeSession.getPlayer(hackerId);
        GamePlayerEntity target = activeSession.getPlayer(request.getTargetPlayerId());

        if (hacker == null || target == null) {
            return HackResult.failure("Player not found", 0);
        }

        // Check if target has shield
        if (activeSession.hasShield(target.getPlayerId())) {
            sendNextQuestion(sessionCode, hackerId);
            return HackResult.failure("Target is shielded!", 0);
        }

        hacker.incrementHackAttempts();

        // Check if guess is correct
        boolean isCorrect = target.getSecretCode().equals(request.getGuessedCode());

        if (isCorrect) {
            hacker.incrementSuccessfulHacks();
            target.incrementTimesHacked();

            // Calculate stolen credits with avatar bonus
            int baseSteal = (int)(target.getCredits() * HACK_STEAL_PERCENTAGE);
            double avatarBonus = getAvatarHackBonus(hacker.getAvatarId());
            int stolen = (int)(baseSteal * avatarBonus);

            // Apply avatar protection for target
            double protection = getAvatarProtection(target.getAvatarId());
            stolen = (int)(stolen * (1 - protection));

            stolen = Math.max(10, stolen);  // Minimum 10 credits

            target.subtractCredits(stolen);
            target.recordCreditsLost(stolen);
            hacker.addCredits(stolen);
            hacker.recordCreditsStolen(stolen);

            playerRepository.save(hacker);
            playerRepository.save(target);

            // Clear hack attempt tracker for this target
            clearHackAttempts(sessionCode, hackerId, target.getPlayerId());

            // Notify the hacked player
            notifyPlayerHacked(sessionCode, target.getPlayerId(), hacker.getStudentName(), stolen);

            // Broadcast event
            GameEvent hackEvent = GameEvent.hackSuccess(sessionCode, hackerId, hacker.getStudentName(),
                target.getStudentName(), stolen);
            broadcastToSession(sessionCode, hackEvent);
            notifyTeacher(sessionCode, hackEvent);

            // Update leaderboard
            broadcastLeaderboard(sessionCode);

            // Move hacker to next question
            sendNextQuestion(sessionCode, hackerId);

            return HackResult.success(target.getStudentName(), stolen, hacker.getCredits());
        } else {
            // Track failed attempt
            int failedAttempts = recordFailedHackAttempt(sessionCode, hackerId, target.getPlayerId());
            playerRepository.save(hacker);

            // Generate hint after 2 failed attempts
            String hint = null;
            if (failedAttempts >= 2) {
                hint = generateHint(target.getSecretCode(), failedAttempts - 1);
            }

            // Move to next question
            sendNextQuestion(sessionCode, hackerId);

            return HackResult.failure(hint, failedAttempts);
        }
    }

    /**
     * End the game session.
     */
    @Transactional
    public GameSessionDto endGame(String sessionCode, String teacherId) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) {
            throw new IllegalArgumentException("Session not found");
        }

        if (!activeSession.getTeacherId().equals(teacherId)) {
            throw new SecurityException("Only the session creator can end the game");
        }

        activeSession.setStatus(GameSessionStatus.ENDED);

        // Update database
        GameSessionEntity entity = activeSession.getSessionEntity();
        entity.setStatus(GameSessionStatus.ENDED);
        entity.setEndedAt(LocalDateTime.now());
        sessionRepository.save(entity);

        // Get final results
        List<PlayerDto> finalLeaderboard = getLeaderboard(sessionCode);

        // Broadcast game end
        GameEvent endEvent = GameEvent.gameEnded(sessionCode, finalLeaderboard);
        broadcastToSession(sessionCode, endEvent);

        // Clean up
        activeSessions.remove(sessionCode);
        hackAttemptTracker.remove(sessionCode);

        logger.info("Game ended for session {}", sessionCode);

        return toDto(entity);
    }

    /**
     * Get current leaderboard.
     */
    public List<PlayerDto> getLeaderboard(String sessionCode) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) {
            return Collections.emptyList();
        }

        List<GamePlayerEntity> players = activeSession.getPlayers().stream()
            .sorted((a, b) -> b.getCredits().compareTo(a.getCredits()))
            .toList();

        List<PlayerDto> leaderboard = new ArrayList<>();
        int rank = 1;
        for (GamePlayerEntity player : players) {
            PlayerDto dto = toPlayerDto(player);
            dto.setRank(rank++);
            leaderboard.add(dto);
        }

        return leaderboard;
    }

    /**
     * Get session info.
     */
    public GameSessionDto getSession(String sessionCode) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession != null) {
            return toDto(activeSession.getSessionEntity());
        }

        return sessionRepository.findById(sessionCode)
            .map(this::toDto)
            .orElse(null);
    }

    /**
     * Get teacher's sessions.
     */
    public List<GameSessionDto> getTeacherSessions(String teacherId) {
        return sessionRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    // Private helper methods

    private String generateSessionCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(SESSION_CODE_LENGTH);
        for (int i = 0; i < SESSION_CODE_LENGTH; i++) {
            code.append(SESSION_CODE_CHARS.charAt(random.nextInt(SESSION_CODE_CHARS.length())));
        }
        return code.toString();
    }

    private void sendNextQuestion(String sessionCode, String playerId) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession == null) return;

        QuestionEntity question = activeSession.getNextQuestion(playerId);
        if (question == null) {
            // No more questions - cycle back
            activeSession.resetQuestionIndex(playerId);
            question = activeSession.getNextQuestion(playerId);
        }

        if (question != null) {
            QuestionDto dto = new QuestionDto();
            dto.setQuestionId(question.getQuestionId());
            dto.setQuestionText(question.getQuestionText());
            dto.setAnswers(Arrays.asList(question.getAllAnswers()));
            dto.setDifficulty(question.getDifficulty());
            dto.setImageUrl(question.getImageUrl());
            dto.setTimeLimitSeconds(15);  // 15 seconds per question

            messagingTemplate.convertAndSendToUser(playerId, "/queue/question", dto);
        }
    }

    private void broadcastToSession(String sessionCode, Object message) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode, message);
    }

    private void notifyTeacher(String sessionCode, Object event) {
        ActiveGameSession activeSession = activeSessions.get(sessionCode);
        if (activeSession != null) {
            messagingTemplate.convertAndSendToUser(
                activeSession.getTeacherId(),
                "/queue/session-events",
                event
            );
        }
    }

    private void notifyTeacherOfAnswer(String sessionCode, GamePlayerEntity player, boolean correct) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", correct ? "ANSWER_CORRECT" : "ANSWER_INCORRECT");
        data.put("playerId", player.getPlayerId());
        data.put("playerName", player.getStudentName());
        data.put("correctAnswers", player.getCorrectAnswers());
        data.put("incorrectAnswers", player.getIncorrectAnswers());
        data.put("accuracy", player.getAccuracy());

        notifyTeacher(sessionCode, data);
    }

    private void notifyPlayerHacked(String sessionCode, String playerId, String hackerName, int creditsLost) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "YOU_WERE_HACKED");
        data.put("hackerName", hackerName);
        data.put("creditsLost", creditsLost);
        data.put("challengeType", selectRandomChallenge());

        messagingTemplate.convertAndSendToUser(playerId, "/queue/hacked", data);
    }

    private void broadcastLeaderboard(String sessionCode) {
        List<LeaderboardEntry> leaderboard = getLeaderboard(sessionCode).stream()
            .map(p -> {
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.setRank(p.getRank());
                entry.setPlayerId(p.getPlayerId());
                entry.setStudentName(p.getStudentName());
                entry.setAvatarId(p.getAvatarId());
                entry.setCredits(p.getCredits());
                entry.setCorrectAnswers(p.getCorrectAnswers());
                entry.setSuccessfulHacks(p.getSuccessfulHacks());
                return entry;
            })
            .toList();

        broadcastToSession(sessionCode, Map.of("type", "LEADERBOARD_UPDATE", "leaderboard", leaderboard));
    }

    private int recordFailedHackAttempt(String sessionCode, String hackerId, String targetId) {
        return hackAttemptTracker
            .computeIfAbsent(sessionCode, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(hackerId, k -> new ConcurrentHashMap<>())
            .merge(targetId, 1, Integer::sum);
    }

    private void clearHackAttempts(String sessionCode, String hackerId, String targetId) {
        Map<String, Map<String, Integer>> sessionAttempts = hackAttemptTracker.get(sessionCode);
        if (sessionAttempts != null) {
            Map<String, Integer> hackerAttempts = sessionAttempts.get(hackerId);
            if (hackerAttempts != null) {
                hackerAttempts.remove(targetId);
            }
        }
    }

    private String generateHint(String secretCode, int revealCount) {
        if (secretCode == null || secretCode.isEmpty()) return null;

        char[] hint = new char[secretCode.length()];
        Arrays.fill(hint, '*');

        // Reveal 'revealCount' digits
        for (int i = 0; i < Math.min(revealCount, secretCode.length()); i++) {
            hint[i] = secretCode.charAt(i);
        }

        return new String(hint);
    }

    private String selectRandomChallenge() {
        String[] challenges = {"MATH_SPRINT", "WORD_SCRAMBLE", "PATTERN_MATCH", "MEMORY_FLASH"};
        return challenges[new Random().nextInt(challenges.length)];
    }

    private int getAvatarCreditBonus(String avatarId) {
        return switch (avatarId) {
            case "CLEVER_CAT" -> 10;
            default -> 0;
        };
    }

    private double getAvatarHackBonus(String avatarId) {
        return switch (avatarId) {
            case "MIGHTY_BEAR" -> 1.5;
            default -> 1.0;
        };
    }

    private double getAvatarProtection(String avatarId) {
        return switch (avatarId) {
            case "SHIELD_TURTLE" -> 0.3;  // 30% protection
            default -> 0.0;
        };
    }

    private GameSessionDto toDto(GameSessionEntity entity) {
        GameSessionDto dto = new GameSessionDto();
        dto.setSessionId(entity.getSessionId());
        dto.setSessionCode(entity.getSessionId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setGameType(entity.getGameType());
        dto.setQuestionSetId(entity.getQuestionSetId());
        dto.setStatus(entity.getStatus());
        dto.setTimeLimitSeconds(entity.getTimeLimitSeconds());
        dto.setTargetCredits(entity.getTargetCredits());
        dto.setPlayerCount(entity.getPlayers().size());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        return dto;
    }

    private PlayerDto toPlayerDto(GamePlayerEntity entity) {
        PlayerDto dto = new PlayerDto();
        dto.setPlayerId(entity.getPlayerId());
        dto.setStudentId(entity.getStudentId());
        dto.setStudentName(entity.getStudentName());
        dto.setAvatarId(entity.getAvatarId());
        dto.setCredits(entity.getCredits());
        dto.setCorrectAnswers(entity.getCorrectAnswers());
        dto.setIncorrectAnswers(entity.getIncorrectAnswers());
        dto.setAccuracy(entity.getAccuracy());
        dto.setHackAttempts(entity.getHackAttempts());
        dto.setSuccessfulHacks(entity.getSuccessfulHacks());
        dto.setHackSuccessRate(entity.getHackSuccessRate());
        dto.setTimesHacked(entity.getTimesHacked());
        dto.setCreditsStolen(entity.getCreditsStolen());
        dto.setCreditsLost(entity.getCreditsLost());
        dto.setConnected(entity.getConnected());
        dto.setJoinedAt(entity.getJoinedAt());
        return dto;
    }

    /**
     * Inner class to track active session state in memory.
     */
    private static class ActiveGameSession {
        private final GameSessionEntity sessionEntity;
        private final List<QuestionEntity> questions;
        private final Map<String, GamePlayerEntity> players = new ConcurrentHashMap<>();
        private final Map<String, Integer> playerQuestionIndex = new ConcurrentHashMap<>();
        private final Map<String, Long> playerShields = new ConcurrentHashMap<>();  // playerId -> shield expiry time
        private GameSessionStatus status;
        private LocalDateTime startedAt;

        public ActiveGameSession(GameSessionEntity entity, List<QuestionEntity> questions) {
            this.sessionEntity = entity;
            this.questions = new ArrayList<>(questions);
            Collections.shuffle(this.questions);  // Randomize question order
            this.status = entity.getStatus();
        }

        public GameSessionEntity getSessionEntity() {
            return sessionEntity;
        }

        public String getTeacherId() {
            return sessionEntity.getTeacherId();
        }

        public String getGameType() {
            return sessionEntity.getGameType();
        }

        public GameSessionStatus getStatus() {
            return status;
        }

        public void setStatus(GameSessionStatus status) {
            this.status = status;
            sessionEntity.setStatus(status);
        }

        public Integer getTimeLimitSeconds() {
            return sessionEntity.getTimeLimitSeconds();
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public void addPlayer(GamePlayerEntity player) {
            players.put(player.getPlayerId(), player);
            playerQuestionIndex.put(player.getPlayerId(), 0);
        }

        public boolean hasPlayer(String studentId) {
            return players.values().stream()
                .anyMatch(p -> p.getStudentId().equals(studentId));
        }

        public GamePlayerEntity getPlayer(String playerId) {
            return players.get(playerId);
        }

        public Collection<GamePlayerEntity> getPlayers() {
            return players.values();
        }

        public QuestionEntity getCurrentQuestion(String playerId) {
            int index = playerQuestionIndex.getOrDefault(playerId, 0);
            if (index < questions.size()) {
                return questions.get(index);
            }
            return null;
        }

        public QuestionEntity getNextQuestion(String playerId) {
            int index = playerQuestionIndex.merge(playerId, 1, Integer::sum) - 1;
            if (index < questions.size()) {
                return questions.get(index);
            }
            return null;
        }

        public void resetQuestionIndex(String playerId) {
            playerQuestionIndex.put(playerId, 0);
        }

        public void setPlayerShield(String playerId, int durationSeconds) {
            long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
            playerShields.put(playerId, expiryTime);
        }

        public boolean hasShield(String playerId) {
            Long expiry = playerShields.get(playerId);
            if (expiry == null) return false;
            if (System.currentTimeMillis() > expiry) {
                playerShields.remove(playerId);
                return false;
            }
            return true;
        }
    }
}
