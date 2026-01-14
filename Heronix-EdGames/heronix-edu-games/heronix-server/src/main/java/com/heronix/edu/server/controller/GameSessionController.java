package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.game.*;
import com.heronix.edu.server.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket controller for multiplayer game sessions.
 * Handles real-time game messages via STOMP protocol.
 */
@Controller
public class GameSessionController {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionController.class);

    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    /**
     * Teacher creates a new game session.
     * Client sends to: /app/session/create
     * Response sent to: /user/queue/session
     */
    @MessageMapping("/session/create")
    @SendToUser("/queue/session")
    public GameSessionDto createSession(@Payload CreateSessionRequest request, Principal principal) {
        logger.info("Creating session for teacher: {}", principal.getName());
        return gameSessionService.createSession(request, principal.getName());
    }

    /**
     * Student joins a game session.
     * Client sends to: /app/session/{sessionCode}/join
     * Response sent to: /user/queue/joined
     */
    @MessageMapping("/session/{sessionCode}/join")
    @SendToUser("/queue/joined")
    public JoinSessionResponse joinSession(@DestinationVariable String sessionCode,
                                            @Payload JoinSessionRequest request,
                                            Principal principal) {
        logger.info("Player {} joining session {}", request.getStudentName(), sessionCode);
        return gameSessionService.joinSession(sessionCode, request);
    }

    /**
     * Teacher starts the game.
     * Client sends to: /app/session/{sessionCode}/start
     * Broadcasts game start to all players via /topic/session/{sessionCode}
     */
    @MessageMapping("/session/{sessionCode}/start")
    public void startGame(@DestinationVariable String sessionCode, Principal principal) {
        logger.info("Starting game for session {}", sessionCode);
        gameSessionService.startGame(sessionCode, principal.getName());
    }

    /**
     * Teacher pauses the game.
     * Client sends to: /app/session/{sessionCode}/pause
     */
    @MessageMapping("/session/{sessionCode}/pause")
    public void pauseGame(@DestinationVariable String sessionCode, Principal principal) {
        logger.info("Pausing game for session {}", sessionCode);
        // TODO: Implement pause functionality
    }

    /**
     * Teacher ends the game.
     * Client sends to: /app/session/{sessionCode}/end
     * Response sent to: /user/queue/session
     */
    @MessageMapping("/session/{sessionCode}/end")
    @SendToUser("/queue/session")
    public GameSessionDto endGame(@DestinationVariable String sessionCode, Principal principal) {
        logger.info("Ending game for session {}", sessionCode);
        return gameSessionService.endGame(sessionCode, principal.getName());
    }

    /**
     * Player submits an answer.
     * Client sends to: /app/session/{sessionCode}/answer
     * Response sent to: /user/queue/answer-result
     */
    @MessageMapping("/session/{sessionCode}/answer")
    @SendToUser("/queue/answer-result")
    public AnswerResult submitAnswer(@DestinationVariable String sessionCode,
                                      @Payload AnswerRequest request,
                                      Principal principal) {
        return gameSessionService.processAnswer(sessionCode, principal.getName(), request);
    }

    /**
     * Player selects a reward after correct answer.
     * Client sends to: /app/session/{sessionCode}/reward
     */
    @MessageMapping("/session/{sessionCode}/reward")
    public void selectReward(@DestinationVariable String sessionCode,
                             @Payload RewardSelectionRequest request,
                             Principal principal) {
        gameSessionService.processRewardSelection(sessionCode, principal.getName(), request.getRewardType());
    }

    /**
     * Player attempts to hack another player.
     * Client sends to: /app/session/{sessionCode}/hack
     * Response sent to: /user/queue/hack-result
     */
    @MessageMapping("/session/{sessionCode}/hack")
    @SendToUser("/queue/hack-result")
    public HackResult attemptHack(@DestinationVariable String sessionCode,
                                   @Payload HackRequest request,
                                   Principal principal) {
        logger.debug("Hack attempt in session {} by {}", sessionCode, principal.getName());
        return gameSessionService.processHack(sessionCode, principal.getName(), request);
    }

    /**
     * Get current leaderboard.
     * Client sends to: /app/session/{sessionCode}/leaderboard
     * Response sent to: /user/queue/leaderboard
     */
    @MessageMapping("/session/{sessionCode}/leaderboard")
    @SendToUser("/queue/leaderboard")
    public List<PlayerDto> getLeaderboard(@DestinationVariable String sessionCode) {
        return gameSessionService.getLeaderboard(sessionCode);
    }

    /**
     * Get session info.
     * Client sends to: /app/session/{sessionCode}/info
     * Response sent to: /user/queue/session-info
     */
    @MessageMapping("/session/{sessionCode}/info")
    @SendToUser("/queue/session-info")
    public GameSessionDto getSessionInfo(@DestinationVariable String sessionCode) {
        return gameSessionService.getSession(sessionCode);
    }
}
