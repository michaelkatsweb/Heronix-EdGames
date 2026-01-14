# Heronix Code Breaker - Multiplayer Educational Game

## Overview

**Code Breaker** is a competitive multiplayer educational game inspired by Blooket's Crypto Hack. Players answer educational questions to earn "Credits" and unlock the ability to "hack" other players by guessing their secret codes. Teachers can start game sessions from the Teacher Portal and monitor real-time student performance metrics.

## Game Flow

### 1. Teacher Starts Session (Teacher Portal)
1. Teacher selects a question set/subject
2. Teacher configures game settings (time limit, difficulty)
3. Teacher clicks "Start Game Session" - generates a session code
4. Students join using the session code from their client

### 2. Player Joins & Setup
1. Student enters session code in the client
2. Student chooses a **Secret Code** (one of 5 options: 4-digit codes like "1234", "5678", etc.)
3. Student selects an **Avatar/Character** that determines bonus multipliers
4. Game begins when teacher starts

### 3. Gameplay Loop
```
[Question Appears]
    → [Answer Correctly] → [Choose Reward: Earn Credits OR Hack Attempt]
    → [Answer Incorrectly] → [No reward, next question]
```

### 4. Hacking Mechanic
- When player chooses "Hack Attempt", they select a target player
- They must guess the target's 4-digit secret code
- **Success**: Steal a percentage of target's credits + bonus based on avatar
- **Failure**: Nothing happens, move to next question
- After 2 failed attempts on same player, hint shown (one correct digit)

### 5. Mini-Challenges (When Hacked)
When a player gets hacked, they must complete a quick mini-challenge:
- **Math Sprint**: Solve 3 quick math problems in 10 seconds
- **Word Scramble**: Unscramble a word in 8 seconds
- **Pattern Match**: Complete the pattern sequence
- **Memory Flash**: Remember and repeat a sequence

Completing the challenge reduces credits lost by 50%.

### 6. Game End
- Timer runs out OR credit goal reached
- Final leaderboard shown
- All stats sent to teacher dashboard

---

## Technical Architecture

### Components

```
┌─────────────────────────────────────────────────────────────────┐
│                      TEACHER PORTAL                              │
│  ┌─────────────────┐  ┌──────────────────────────────────────┐  │
│  │ Session Control │  │     Real-Time Metrics Dashboard      │  │
│  │  - Start/Stop   │  │  - Live Leaderboard                  │  │
│  │  - Settings     │  │  - Per-Student Stats                 │  │
│  │  - Question Set │  │  - Correct/Wrong Answers             │  │
│  └────────┬────────┘  │  - Credits Earned/Stolen             │  │
│           │           │  - Hack Success Rate                 │  │
│           │           └──────────────────────────────────────┘  │
└───────────┼─────────────────────────────────────────────────────┘
            │ WebSocket
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      HERONIX SERVER                              │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                  Game Session Manager                       │ │
│  │  - Active Sessions Map                                      │ │
│  │  - Player States                                            │ │
│  │  - Question Queue                                           │ │
│  │  - Score Calculation                                        │ │
│  │  - Event Broadcasting                                       │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              WebSocket Message Broker                       │ │
│  │  - Teacher Channel (session control, metrics)               │ │
│  │  - Player Channel (game events, questions)                  │ │
│  │  - Broadcast Channel (leaderboard updates)                  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
            │ WebSocket
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     STUDENT CLIENT                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Join Session   │  │   Game View     │  │  Hack Panel     │  │
│  │  - Enter Code   │  │  - Question     │  │  - Target List  │  │
│  │  - Pick Secret  │  │  - Timer        │  │  - Code Input   │  │
│  │  - Pick Avatar  │  │  - Credits      │  │  - Hints        │  │
│  └─────────────────┘  │  - Leaderboard  │  └─────────────────┘  │
│                       └─────────────────┘                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Server Implementation

### 1. WebSocket Configuration

```java
// WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/game")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

### 2. Game Session Entity

```java
@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    private String sessionId;          // 6-char code like "ABC123"
    private String teacherId;
    private String questionSetId;
    private GameStatus status;         // WAITING, ACTIVE, PAUSED, ENDED
    private Integer timeLimit;         // seconds
    private Integer targetCredits;     // win condition
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "session")
    private List<GamePlayer> players;
}

@Entity
@Table(name = "game_players")
public class GamePlayer {
    @Id
    private String odplayerId;
    private String sessionId;
    private String studentId;
    private String studentName;
    private String secretCode;         // e.g., "1234"
    private String avatarId;
    private Integer credits;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer hackAttempts;
    private Integer successfulHacks;
    private Integer timesHacked;
    private Boolean connected;
    private LocalDateTime joinedAt;
}
```

### 3. Game Session Controller

```java
@Controller
public class GameSessionController {

    private final GameSessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    // Teacher creates session
    @MessageMapping("/session/create")
    @SendToUser("/queue/session")
    public GameSessionDto createSession(CreateSessionRequest request, Principal principal) {
        return sessionService.createSession(request, principal.getName());
    }

    // Teacher starts game
    @MessageMapping("/session/{sessionId}/start")
    public void startGame(@DestinationVariable String sessionId, Principal principal) {
        sessionService.startGame(sessionId, principal.getName());
        broadcastGameStart(sessionId);
    }

    // Student joins session
    @MessageMapping("/session/{sessionId}/join")
    @SendToUser("/queue/joined")
    public JoinResult joinSession(@DestinationVariable String sessionId,
                                   JoinRequest request) {
        return sessionService.joinSession(sessionId, request);
    }

    // Player submits answer
    @MessageMapping("/session/{sessionId}/answer")
    @SendToUser("/queue/answer-result")
    public AnswerResult submitAnswer(@DestinationVariable String sessionId,
                                      AnswerRequest request, Principal principal) {
        AnswerResult result = sessionService.processAnswer(sessionId, request, principal);
        broadcastLeaderboard(sessionId);
        broadcastToTeacher(sessionId, result);
        return result;
    }

    // Player attempts hack
    @MessageMapping("/session/{sessionId}/hack")
    @SendToUser("/queue/hack-result")
    public HackResult attemptHack(@DestinationVariable String sessionId,
                                   HackRequest request, Principal principal) {
        HackResult result = sessionService.processHack(sessionId, request, principal);
        if (result.isSuccess()) {
            notifyHackedPlayer(result);
        }
        broadcastLeaderboard(sessionId);
        broadcastToTeacher(sessionId, result);
        return result;
    }

    // Broadcast leaderboard to all players
    private void broadcastLeaderboard(String sessionId) {
        List<LeaderboardEntry> leaderboard = sessionService.getLeaderboard(sessionId);
        messagingTemplate.convertAndSend(
            "/topic/session/" + sessionId + "/leaderboard",
            leaderboard
        );
    }

    // Send real-time metrics to teacher
    private void broadcastToTeacher(String sessionId, Object event) {
        String teacherId = sessionService.getTeacherId(sessionId);
        messagingTemplate.convertAndSendToUser(
            teacherId,
            "/queue/metrics",
            new MetricsEvent(sessionId, event)
        );
    }
}
```

### 4. Game Session Service

```java
@Service
public class GameSessionService {

    private final Map<String, ActiveSession> activeSessions = new ConcurrentHashMap<>();
    private final GameSessionRepository sessionRepository;
    private final QuestionService questionService;

    public GameSessionDto createSession(CreateSessionRequest request, String teacherId) {
        String sessionCode = generateSessionCode(); // e.g., "XK7M2P"

        GameSession session = new GameSession();
        session.setSessionId(sessionCode);
        session.setTeacherId(teacherId);
        session.setQuestionSetId(request.getQuestionSetId());
        session.setTimeLimit(request.getTimeLimit());
        session.setStatus(GameStatus.WAITING);

        sessionRepository.save(session);

        // Load questions into memory
        List<Question> questions = questionService.getQuestions(request.getQuestionSetId());
        ActiveSession active = new ActiveSession(session, questions);
        activeSessions.put(sessionCode, active);

        return toDto(session);
    }

    public JoinResult joinSession(String sessionId, JoinRequest request) {
        ActiveSession session = activeSessions.get(sessionId);
        if (session == null) {
            return JoinResult.error("Session not found");
        }
        if (session.getStatus() != GameStatus.WAITING) {
            return JoinResult.error("Game already started");
        }

        GamePlayer player = new GamePlayer();
        player.setPlayerId(UUID.randomUUID().toString());
        player.setSessionId(sessionId);
        player.setStudentId(request.getStudentId());
        player.setStudentName(request.getStudentName());
        player.setSecretCode(request.getSecretCode());
        player.setAvatarId(request.getAvatarId());
        player.setCredits(0);
        player.setConnected(true);

        session.addPlayer(player);

        return JoinResult.success(player);
    }

    public AnswerResult processAnswer(String sessionId, AnswerRequest request, Principal principal) {
        ActiveSession session = activeSessions.get(sessionId);
        GamePlayer player = session.getPlayer(principal.getName());
        Question question = session.getCurrentQuestion(player.getPlayerId());

        boolean correct = question.getCorrectAnswer().equals(request.getAnswer());

        if (correct) {
            player.incrementCorrectAnswers();
            // Return reward options
            return AnswerResult.correct(Arrays.asList(
                new RewardOption("CREDITS", "Earn 50 Credits", 50),
                new RewardOption("HACK", "Attempt to Hack", 0),
                new RewardOption("SHIELD", "Protect yourself for 30s", 0)
            ));
        } else {
            player.incrementIncorrectAnswers();
            return AnswerResult.incorrect();
        }
    }

    public HackResult processHack(String sessionId, HackRequest request, Principal principal) {
        ActiveSession session = activeSessions.get(sessionId);
        GamePlayer hacker = session.getPlayer(principal.getName());
        GamePlayer target = session.getPlayer(request.getTargetPlayerId());

        hacker.incrementHackAttempts();

        if (target.getSecretCode().equals(request.getGuessedCode())) {
            // Successful hack!
            hacker.incrementSuccessfulHacks();
            target.incrementTimesHacked();

            // Calculate stolen credits based on avatar bonus
            int baseSteal = (int)(target.getCredits() * 0.25); // 25% of target's credits
            int avatarBonus = getAvatarBonus(hacker.getAvatarId());
            int stolen = baseSteal * avatarBonus;

            target.subtractCredits(stolen);
            hacker.addCredits(stolen);

            return HackResult.success(stolen, target.getStudentName());
        } else {
            // Track failed attempts for hints
            session.recordFailedAttempt(hacker.getPlayerId(), target.getPlayerId());
            int failedAttempts = session.getFailedAttempts(hacker.getPlayerId(), target.getPlayerId());

            String hint = null;
            if (failedAttempts >= 2) {
                // Give hint: reveal one digit
                hint = getHint(target.getSecretCode(), failedAttempts - 1);
            }

            return HackResult.failure(hint);
        }
    }

    public List<LeaderboardEntry> getLeaderboard(String sessionId) {
        ActiveSession session = activeSessions.get(sessionId);
        return session.getPlayers().stream()
            .sorted((a, b) -> b.getCredits() - a.getCredits())
            .limit(10)
            .map(p -> new LeaderboardEntry(
                p.getStudentName(),
                p.getCredits(),
                p.getAvatarId()
            ))
            .toList();
    }
}
```

---

## Teacher Portal Implementation

### 1. Game Session Control View (FXML)

```xml
<!-- GameSessionControl.fxml -->
<BorderPane>
    <top>
        <HBox styleClass="header">
            <Label text="Code Breaker - Game Control" styleClass="title"/>
        </HBox>
    </top>

    <left>
        <!-- Session Setup Panel -->
        <VBox styleClass="setup-panel" spacing="15">
            <Label text="Create New Session" styleClass="section-title"/>

            <Label text="Question Set:"/>
            <ComboBox fx:id="questionSetCombo"/>

            <Label text="Time Limit (minutes):"/>
            <Spinner fx:id="timeLimitSpinner" min="5" max="30" value="10"/>

            <Label text="Target Credits:"/>
            <Spinner fx:id="targetCreditsSpinner" min="500" max="5000" value="1000"/>

            <Button fx:id="createSessionBtn" text="Create Session" onAction="#handleCreateSession"/>

            <Separator/>

            <VBox fx:id="sessionInfoBox" visible="false">
                <Label text="Session Code:" styleClass="label"/>
                <Label fx:id="sessionCodeLabel" styleClass="session-code"/>
                <Label fx:id="playerCountLabel"/>

                <HBox spacing="10">
                    <Button fx:id="startGameBtn" text="Start Game" onAction="#handleStartGame"/>
                    <Button fx:id="pauseGameBtn" text="Pause" onAction="#handlePauseGame"/>
                    <Button fx:id="endGameBtn" text="End Game" onAction="#handleEndGame"/>
                </HBox>
            </VBox>
        </VBox>
    </left>

    <center>
        <!-- Real-Time Metrics Dashboard -->
        <VBox styleClass="metrics-panel" spacing="20">
            <Label text="Live Metrics" styleClass="section-title"/>

            <!-- Summary Cards -->
            <HBox spacing="15">
                <VBox styleClass="metric-card">
                    <Label text="Players"/>
                    <Label fx:id="playerCountMetric" styleClass="metric-value"/>
                </VBox>
                <VBox styleClass="metric-card">
                    <Label text="Questions Answered"/>
                    <Label fx:id="questionsAnsweredMetric" styleClass="metric-value"/>
                </VBox>
                <VBox styleClass="metric-card">
                    <Label text="Avg Accuracy"/>
                    <Label fx:id="avgAccuracyMetric" styleClass="metric-value"/>
                </VBox>
                <VBox styleClass="metric-card">
                    <Label text="Total Hacks"/>
                    <Label fx:id="totalHacksMetric" styleClass="metric-value"/>
                </VBox>
            </HBox>

            <!-- Leaderboard -->
            <Label text="Live Leaderboard" styleClass="subsection-title"/>
            <TableView fx:id="leaderboardTable">
                <columns>
                    <TableColumn text="Rank" fx:id="rankColumn"/>
                    <TableColumn text="Student" fx:id="studentColumn"/>
                    <TableColumn text="Credits" fx:id="creditsColumn"/>
                    <TableColumn text="Correct" fx:id="correctColumn"/>
                    <TableColumn text="Wrong" fx:id="wrongColumn"/>
                    <TableColumn text="Hacks" fx:id="hacksColumn"/>
                </columns>
            </TableView>

            <!-- Activity Feed -->
            <Label text="Live Activity" styleClass="subsection-title"/>
            <ListView fx:id="activityFeed" prefHeight="150"/>
        </VBox>
    </center>

    <right>
        <!-- Per-Student Detail Panel -->
        <VBox fx:id="studentDetailPanel" styleClass="detail-panel" spacing="10">
            <Label text="Student Details" styleClass="section-title"/>
            <Label fx:id="selectedStudentName" styleClass="student-name"/>

            <GridPane>
                <Label text="Credits:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
                <Label fx:id="detailCredits" GridPane.rowIndex="0" GridPane.columnIndex="1"/>

                <Label text="Correct Answers:" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
                <Label fx:id="detailCorrect" GridPane.rowIndex="1" GridPane.columnIndex="1"/>

                <Label text="Wrong Answers:" GridPane.rowIndex="2" GridPane.columnIndex="0"/>
                <Label fx:id="detailWrong" GridPane.rowIndex="2" GridPane.columnIndex="1"/>

                <Label text="Accuracy:" GridPane.rowIndex="3" GridPane.columnIndex="0"/>
                <Label fx:id="detailAccuracy" GridPane.rowIndex="3" GridPane.columnIndex="1"/>

                <Label text="Hack Attempts:" GridPane.rowIndex="4" GridPane.columnIndex="0"/>
                <Label fx:id="detailHackAttempts" GridPane.rowIndex="4" GridPane.columnIndex="1"/>

                <Label text="Successful Hacks:" GridPane.rowIndex="5" GridPane.columnIndex="0"/>
                <Label fx:id="detailSuccessfulHacks" GridPane.rowIndex="5" GridPane.columnIndex="1"/>

                <Label text="Times Hacked:" GridPane.rowIndex="6" GridPane.columnIndex="0"/>
                <Label fx:id="detailTimesHacked" GridPane.rowIndex="6" GridPane.columnIndex="1"/>
            </GridPane>

            <!-- Student's Answer History -->
            <Label text="Recent Answers"/>
            <ListView fx:id="studentAnswerHistory" prefHeight="100"/>
        </VBox>
    </right>
</BorderPane>
```

### 2. Game Session Controller (Teacher Portal)

```java
public class GameSessionController implements Initializable {

    @FXML private ComboBox<QuestionSet> questionSetCombo;
    @FXML private Spinner<Integer> timeLimitSpinner;
    @FXML private Label sessionCodeLabel;
    @FXML private Label playerCountLabel;
    @FXML private TableView<PlayerMetrics> leaderboardTable;
    @FXML private ListView<String> activityFeed;

    private WebSocketClient webSocketClient;
    private String currentSessionId;
    private ObservableList<PlayerMetrics> playerMetrics = FXCollections.observableArrayList();
    private ObservableList<String> activities = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWebSocket();
        loadQuestionSets();
        leaderboardTable.setItems(playerMetrics);
        activityFeed.setItems(activities);
    }

    private void setupWebSocket() {
        webSocketClient = new WebSocketClient("ws://localhost:8080/ws/game");

        webSocketClient.subscribe("/user/queue/metrics", message -> {
            Platform.runLater(() -> handleMetricsUpdate(message));
        });

        webSocketClient.subscribe("/user/queue/session", message -> {
            Platform.runLater(() -> handleSessionUpdate(message));
        });
    }

    @FXML
    public void handleCreateSession() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setQuestionSetId(questionSetCombo.getValue().getId());
        request.setTimeLimit(timeLimitSpinner.getValue() * 60);

        webSocketClient.send("/app/session/create", request);
    }

    @FXML
    public void handleStartGame() {
        webSocketClient.send("/app/session/" + currentSessionId + "/start", null);
    }

    private void handleMetricsUpdate(MetricsEvent event) {
        // Update leaderboard
        if (event.getLeaderboard() != null) {
            playerMetrics.clear();
            playerMetrics.addAll(event.getLeaderboard());
        }

        // Add to activity feed
        if (event.getActivity() != null) {
            activities.add(0, formatActivity(event.getActivity()));
            if (activities.size() > 50) {
                activities.remove(activities.size() - 1);
            }
        }

        // Update summary metrics
        updateSummaryCards(event);
    }

    private String formatActivity(ActivityEvent activity) {
        return switch (activity.getType()) {
            case "CORRECT_ANSWER" -> String.format("%s answered correctly (+%d credits)",
                activity.getStudentName(), activity.getCredits());
            case "WRONG_ANSWER" -> String.format("%s answered incorrectly",
                activity.getStudentName());
            case "HACK_SUCCESS" -> String.format("%s hacked %s for %d credits!",
                activity.getStudentName(), activity.getTargetName(), activity.getCredits());
            case "HACK_FAIL" -> String.format("%s failed to hack %s",
                activity.getStudentName(), activity.getTargetName());
            case "PLAYER_JOINED" -> String.format("%s joined the game",
                activity.getStudentName());
            default -> activity.toString();
        };
    }
}
```

---

## Client Implementation (heronix-client)

### 1. Code Breaker Game View

```java
public class CodeBreakerGameController {

    @FXML private Label questionLabel;
    @FXML private VBox answersBox;
    @FXML private Label timerLabel;
    @FXML private Label creditsLabel;
    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private VBox rewardPanel;
    @FXML private VBox hackPanel;
    @FXML private ListView<PlayerTarget> targetList;
    @FXML private TextField codeInput;
    @FXML private Label hintLabel;

    private WebSocketClient webSocketClient;
    private String sessionId;
    private String playerId;
    private Timeline gameTimer;

    public void joinSession(String code, String studentId, String secretCode, String avatar) {
        this.sessionId = code;

        webSocketClient = new WebSocketClient("ws://server:8080/ws/game");

        // Subscribe to game events
        webSocketClient.subscribe("/topic/session/" + code + "/question", this::onNewQuestion);
        webSocketClient.subscribe("/topic/session/" + code + "/leaderboard", this::onLeaderboardUpdate);
        webSocketClient.subscribe("/user/queue/answer-result", this::onAnswerResult);
        webSocketClient.subscribe("/user/queue/hack-result", this::onHackResult);
        webSocketClient.subscribe("/user/queue/hacked", this::onBeingHacked);

        // Join session
        JoinRequest request = new JoinRequest(studentId, secretCode, avatar);
        webSocketClient.send("/app/session/" + code + "/join", request);
    }

    private void onNewQuestion(Question question) {
        Platform.runLater(() -> {
            questionLabel.setText(question.getText());
            answersBox.getChildren().clear();

            for (String answer : question.getAnswers()) {
                Button btn = new Button(answer);
                btn.setOnAction(e -> submitAnswer(answer));
                btn.getStyleClass().add("answer-button");
                answersBox.getChildren().add(btn);
            }
        });
    }

    private void submitAnswer(String answer) {
        AnswerRequest request = new AnswerRequest(answer);
        webSocketClient.send("/app/session/" + sessionId + "/answer", request);
    }

    private void onAnswerResult(AnswerResult result) {
        Platform.runLater(() -> {
            if (result.isCorrect()) {
                showRewardOptions(result.getRewardOptions());
            } else {
                showIncorrectFeedback();
                requestNextQuestion();
            }
        });
    }

    private void showRewardOptions(List<RewardOption> options) {
        rewardPanel.setVisible(true);
        rewardPanel.getChildren().clear();

        for (RewardOption option : options) {
            Button btn = new Button(option.getLabel());
            btn.setOnAction(e -> selectReward(option));
            rewardPanel.getChildren().add(btn);
        }
    }

    private void selectReward(RewardOption option) {
        rewardPanel.setVisible(false);

        if ("HACK".equals(option.getType())) {
            showHackPanel();
        } else if ("CREDITS".equals(option.getType())) {
            addCredits(option.getValue());
            requestNextQuestion();
        }
    }

    private void showHackPanel() {
        hackPanel.setVisible(true);
        // Load list of other players as targets
        loadTargets();
    }

    @FXML
    public void handleHackAttempt() {
        PlayerTarget target = targetList.getSelectionModel().getSelectedItem();
        String guessedCode = codeInput.getText();

        if (target == null || guessedCode.length() != 4) {
            return;
        }

        HackRequest request = new HackRequest(target.getPlayerId(), guessedCode);
        webSocketClient.send("/app/session/" + sessionId + "/hack", request);

        hackPanel.setVisible(false);
        codeInput.clear();
    }

    private void onHackResult(HackResult result) {
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                showHackSuccess(result.getStolenCredits(), result.getTargetName());
            } else {
                if (result.getHint() != null) {
                    hintLabel.setText("Hint: " + result.getHint());
                }
                showHackFailed();
            }
            requestNextQuestion();
        });
    }

    private void onBeingHacked(HackedEvent event) {
        Platform.runLater(() -> {
            // Show mini-challenge to reduce damage
            showMiniChallenge(event.getChallengeType(), event.getStolenCredits());
        });
    }

    private void onLeaderboardUpdate(List<LeaderboardEntry> entries) {
        Platform.runLater(() -> {
            leaderboardTable.getItems().clear();
            leaderboardTable.getItems().addAll(entries);
        });
    }
}
```

---

## Question Sets

Questions can be organized by subject and grade level:

```java
@Entity
@Table(name = "question_sets")
public class QuestionSet {
    @Id
    private String setId;
    private String name;
    private String subject;        // MATH, SCIENCE, READING, etc.
    private String gradeLevel;     // K-2, 3-5, 6-8, etc.
    private String createdBy;      // teacherId

    @OneToMany(mappedBy = "questionSet")
    private List<Question> questions;
}

@Entity
@Table(name = "questions")
public class Question {
    @Id
    private String questionId;
    private String questionSetId;
    private String text;
    private String correctAnswer;

    @ElementCollection
    private List<String> wrongAnswers;

    private Integer difficulty;    // 1-5
    private String imageUrl;       // optional
}
```

---

## Avatars/Characters

Each avatar provides different bonuses:

| Avatar | Bonus Effect |
|--------|--------------|
| Rookie Robot | No bonus (default) |
| Clever Cat | +10 bonus credits per correct answer |
| Swift Fox | 20% faster question timer |
| Mighty Bear | 50% more credits when hacking |
| Shield Turtle | 30% protection against hacks |
| Lucky Unicorn | 10% chance for double rewards |

```java
public enum Avatar {
    ROOKIE_ROBOT("rookie_robot", 1.0, 1.0, 1.0, 0),
    CLEVER_CAT("clever_cat", 1.0, 1.0, 1.0, 10),
    SWIFT_FOX("swift_fox", 1.0, 1.0, 1.2, 0),
    MIGHTY_BEAR("mighty_bear", 1.5, 1.0, 1.0, 0),
    SHIELD_TURTLE("shield_turtle", 1.0, 0.7, 1.0, 0),
    LUCKY_UNICORN("lucky_unicorn", 1.0, 1.0, 1.0, 0); // special handling

    private final String id;
    private final double hackBonus;
    private final double hackProtection;
    private final double speedBonus;
    private final int creditBonus;
}
```

---

## Implementation Phases

### Phase 1: Server Infrastructure
1. Add Spring WebSocket dependencies
2. Create WebSocket configuration
3. Implement GameSession and GamePlayer entities
4. Create GameSessionService with in-memory session management
5. Implement GameSessionController with WebSocket handlers

### Phase 2: Teacher Portal
1. Create GameSessionControl.fxml view
2. Implement GameSessionController
3. Add WebSocket client for real-time updates
4. Build metrics dashboard with live leaderboard
5. Add activity feed and per-student details

### Phase 3: Client Game
1. Create CodeBreakerGame module in heronix-games
2. Implement join session flow with code entry
3. Build question/answer UI
4. Implement reward selection panel
5. Create hack panel with target selection
6. Add mini-challenges for defense
7. Implement leaderboard display

### Phase 4: Polish
1. Add animations and sound effects
2. Implement avatar bonuses
3. Add question set management for teachers
4. Create post-game summary and export
5. Testing and bug fixes

---

## API Endpoints Summary

### REST Endpoints
- `POST /api/game/question-sets` - Create question set
- `GET /api/game/question-sets` - List teacher's question sets
- `POST /api/game/question-sets/{id}/questions` - Add questions
- `GET /api/game/sessions/{sessionId}/results` - Get final results

### WebSocket Endpoints
- `/app/session/create` - Create new session (teacher)
- `/app/session/{id}/start` - Start game (teacher)
- `/app/session/{id}/pause` - Pause game (teacher)
- `/app/session/{id}/end` - End game (teacher)
- `/app/session/{id}/join` - Join session (student)
- `/app/session/{id}/answer` - Submit answer (student)
- `/app/session/{id}/hack` - Attempt hack (student)
- `/topic/session/{id}/question` - Broadcast questions
- `/topic/session/{id}/leaderboard` - Broadcast leaderboard
- `/user/queue/metrics` - Teacher metrics updates
- `/user/queue/answer-result` - Answer result to player
- `/user/queue/hack-result` - Hack result to player
- `/user/queue/hacked` - Notification when hacked

---

## Security Considerations

1. **Session Validation**: Verify student is registered and approved before joining
2. **Anti-Cheat**: Server-side validation of all answers and actions
3. **Rate Limiting**: Prevent spam of answer/hack attempts
4. **Session Isolation**: Players can only interact within their session
5. **Teacher Authorization**: Only session creator can control the game
