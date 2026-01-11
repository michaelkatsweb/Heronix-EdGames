package com.heronix.edu.server.service;

import com.heronix.edu.common.model.GameScore;
import com.heronix.edu.server.entity.GameScoreEntity;
import com.heronix.edu.server.repository.GameScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for educational reporting and analytics.
 * Provides student progress, class performance, and game statistics.
 */
@Service
public class ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);

    @Autowired
    private GameScoreRepository gameScoreRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Get student performance summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentPerformance(String studentId) {
        logger.debug("Generating performance report for student: {}", studentId);

        List<GameScoreEntity> scores = gameScoreRepository.findByStudentId(studentId);

        if (scores.isEmpty()) {
            return Map.of(
                    "studentId", studentId,
                    "totalGamesPlayed", 0,
                    "message", "No games played yet"
            );
        }

        int totalGames = scores.size();
        int completedGames = (int) scores.stream().filter(GameScoreEntity::getCompleted).count();
        double averageScore = scores.stream()
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0);
        double averageAccuracy = calculateAverageAccuracy(scores);
        int totalTimeMinutes = scores.stream()
                .mapToInt(s -> s.getTimeSeconds() != null ? s.getTimeSeconds() : 0)
                .sum() / 60;

        // Game-specific statistics
        Map<String, GameStats> gameStats = scores.stream()
                .collect(Collectors.groupingBy(
                        GameScoreEntity::getGameId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateGameStats
                        )
                ));

        auditService.logStudentDataAccess("SYSTEM", studentId, "PERFORMANCE_REPORT");

        Map<String, Object> performance = new HashMap<>();
        performance.put("studentId", studentId);
        performance.put("totalGamesPlayed", totalGames);
        performance.put("completedGames", completedGames);
        performance.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        performance.put("averageAccuracy", Math.round(averageAccuracy * 100.0) / 100.0);
        performance.put("totalTimeMinutes", totalTimeMinutes);
        performance.put("gameBreakdown", gameStats);
        performance.put("recentScores", getRecentScores(scores, 10));

        return performance;
    }

    /**
     * Get student progress over time
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentProgress(String studentId, String gameId, int days) {
        logger.debug("Getting progress for student: {}, game: {}, last {} days", studentId, gameId, days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<GameScoreEntity> scores = gameScoreRepository.findByStudentIdAndDateRange(
                studentId, since, LocalDateTime.now()
        );

        List<GameScoreEntity> gameScores = gameId != null ?
                scores.stream().filter(s -> s.getGameId().equals(gameId)).collect(Collectors.toList()) :
                scores;

        if (gameScores.isEmpty()) {
            return Map.of(
                    "studentId", studentId,
                    "gameId", gameId != null ? gameId : "all",
                    "message", "No data for the specified period"
            );
        }

        // Calculate trend
        List<Map<String, Object>> timeline = gameScores.stream()
                .sorted(Comparator.comparing(GameScoreEntity::getPlayedAt))
                .map(score -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", score.getPlayedAt().toLocalDate().toString());
                    point.put("score", score.getScore());
                    point.put("accuracy", calculateAccuracy(score));
                    point.put("gameId", score.getGameId());
                    return point;
                })
                .collect(Collectors.toList());

        double improvement = calculateImprovement(gameScores);

        Map<String, Object> progress = new HashMap<>();
        progress.put("studentId", studentId);
        progress.put("gameId", gameId != null ? gameId : "all");
        progress.put("periodDays", days);
        progress.put("totalAttempts", gameScores.size());
        progress.put("improvement", Math.round(improvement * 100.0) / 100.0);
        progress.put("timeline", timeline);
        progress.put("currentAverage", gameScores.stream()
                .limit(5)
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0));

        return progress;
    }

    /**
     * Get class performance summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClassPerformance(List<String> studentIds, String gameId) {
        logger.debug("Generating class performance report for {} students", studentIds.size());

        List<GameScoreEntity> allScores = studentIds.stream()
                .flatMap(studentId -> gameScoreRepository.findByStudentId(studentId).stream())
                .collect(Collectors.toList());

        if (gameId != null) {
            allScores = allScores.stream()
                    .filter(score -> score.getGameId().equals(gameId))
                    .collect(Collectors.toList());
        }

        if (allScores.isEmpty()) {
            return Map.of(
                    "totalStudents", studentIds.size(),
                    "message", "No data available"
            );
        }

        // Calculate class statistics
        double classAverage = allScores.stream()
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0);

        double classAccuracy = calculateAverageAccuracy(allScores);

        int completedCount = (int) allScores.stream()
                .filter(GameScoreEntity::getCompleted)
                .count();

        // Student rankings
        Map<String, Double> studentAverages = allScores.stream()
                .collect(Collectors.groupingBy(
                        GameScoreEntity::getStudentId,
                        Collectors.averagingInt(GameScoreEntity::getScore)
                ));

        List<Map<String, Object>> topStudents = studentAverages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> student = new HashMap<>();
                    student.put("studentId", entry.getKey());
                    student.put("averageScore", Math.round(entry.getValue() * 100.0) / 100.0);
                    return student;
                })
                .collect(Collectors.toList());

        Map<String, Object> classPerformance = new HashMap<>();
        classPerformance.put("totalStudents", studentIds.size());
        classPerformance.put("totalScores", allScores.size());
        classPerformance.put("classAverage", Math.round(classAverage * 100.0) / 100.0);
        classPerformance.put("classAccuracy", Math.round(classAccuracy * 100.0) / 100.0);
        classPerformance.put("completionRate", Math.round((double) completedCount / allScores.size() * 100.0));
        classPerformance.put("topPerformers", topStudents);
        classPerformance.put("gameId", gameId != null ? gameId : "all");

        return classPerformance;
    }

    /**
     * Get game statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGameStatistics(String gameId) {
        logger.debug("Getting statistics for game: {}", gameId);

        List<GameScoreEntity> scores = gameScoreRepository.findByStudentIdAndGameId("", gameId);

        if (scores.isEmpty()) {
            scores = gameScoreRepository.findAll().stream()
                    .filter(s -> s.getGameId().equals(gameId))
                    .collect(Collectors.toList());
        }

        if (scores.isEmpty()) {
            return Map.of(
                    "gameId", gameId,
                    "message", "No data available for this game"
            );
        }

        long uniquePlayers = scores.stream()
                .map(GameScoreEntity::getStudentId)
                .distinct()
                .count();

        double averageScore = scores.stream()
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0);

        int highScore = scores.stream()
                .mapToInt(GameScoreEntity::getScore)
                .max()
                .orElse(0);

        double averageTime = scores.stream()
                .filter(s -> s.getTimeSeconds() != null && s.getTimeSeconds() > 0)
                .mapToInt(GameScoreEntity::getTimeSeconds)
                .average()
                .orElse(0.0);

        // Difficulty distribution
        Map<String, Long> difficultyDistribution = scores.stream()
                .filter(s -> s.getDifficultyLevel() != null)
                .collect(Collectors.groupingBy(
                        GameScoreEntity::getDifficultyLevel,
                        Collectors.counting()
                ));

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("gameId", gameId);
        statistics.put("totalPlays", scores.size());
        statistics.put("uniquePlayers", uniquePlayers);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("highScore", highScore);
        statistics.put("averageTimeSeconds", Math.round(averageTime));
        statistics.put("difficultyDistribution", difficultyDistribution);

        return statistics;
    }

    /**
     * Get leaderboard for a game
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaderboard(String gameId, int limit) {
        logger.debug("Getting leaderboard for game: {}, limit: {}", gameId, limit);

        List<GameScoreEntity> scores = gameScoreRepository.findAll().stream()
                .filter(s -> s.getGameId().equals(gameId))
                .collect(Collectors.toList());

        // Get best score per student
        Map<String, GameScoreEntity> bestScores = scores.stream()
                .collect(Collectors.toMap(
                        GameScoreEntity::getStudentId,
                        score -> score,
                        (s1, s2) -> s1.getScore() > s2.getScore() ? s1 : s2
                ));

        return bestScores.values().stream()
                .sorted(Comparator.comparingInt(GameScoreEntity::getScore).reversed())
                .limit(limit)
                .map(score -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("studentId", score.getStudentId());
                    entry.put("score", score.getScore());
                    entry.put("maxScore", score.getMaxScore());
                    entry.put("accuracy", calculateAccuracy(score));
                    entry.put("playedAt", score.getPlayedAt().toString());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // Helper methods

    private double calculateAverageAccuracy(List<GameScoreEntity> scores) {
        return scores.stream()
                .filter(s -> s.getCorrectAnswers() != null && s.getIncorrectAnswers() != null)
                .mapToDouble(this::calculateAccuracy)
                .average()
                .orElse(0.0);
    }

    private double calculateAccuracy(GameScoreEntity score) {
        if (score.getCorrectAnswers() == null || score.getIncorrectAnswers() == null) {
            return 0.0;
        }
        int total = score.getCorrectAnswers() + score.getIncorrectAnswers();
        if (total == 0) return 0.0;
        return (double) score.getCorrectAnswers() / total * 100.0;
    }

    private GameStats calculateGameStats(List<GameScoreEntity> scores) {
        double avgScore = scores.stream().mapToInt(GameScoreEntity::getScore).average().orElse(0.0);
        int plays = scores.size();
        int bestScore = scores.stream().mapToInt(GameScoreEntity::getScore).max().orElse(0);

        return new GameStats(
                plays,
                Math.round(avgScore * 100.0) / 100.0,
                bestScore
        );
    }

    private List<Map<String, Object>> getRecentScores(List<GameScoreEntity> scores, int limit) {
        return scores.stream()
                .sorted(Comparator.comparing(GameScoreEntity::getPlayedAt).reversed())
                .limit(limit)
                .map(score -> {
                    Map<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put("gameId", score.getGameId());
                    scoreMap.put("score", score.getScore());
                    scoreMap.put("playedAt", score.getPlayedAt().toString());
                    scoreMap.put("completed", score.getCompleted());
                    return scoreMap;
                })
                .collect(Collectors.toList());
    }

    private double calculateImprovement(List<GameScoreEntity> scores) {
        if (scores.size() < 2) return 0.0;

        List<GameScoreEntity> sorted = scores.stream()
                .sorted(Comparator.comparing(GameScoreEntity::getPlayedAt))
                .collect(Collectors.toList());

        double firstAvg = sorted.stream().limit(Math.min(3, sorted.size()))
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0);

        double recentAvg = sorted.stream()
                .skip(Math.max(0, sorted.size() - 3))
                .mapToInt(GameScoreEntity::getScore)
                .average()
                .orElse(0.0);

        return recentAvg - firstAvg;
    }

    // Inner class for game statistics
    private record GameStats(int plays, double averageScore, int bestScore) {
    }
}
