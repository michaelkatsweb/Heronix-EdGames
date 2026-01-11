package com.heronix.edu.server.controller;

import com.heronix.edu.server.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for reporting and analytics.
 * Provides student progress, class performance, and game statistics.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);

    @Autowired
    private ReportingService reportingService;

    /**
     * Get student performance summary
     * GET /api/reports/student/{studentId}/performance
     */
    @GetMapping("/student/{studentId}/performance")
    public ResponseEntity<Map<String, Object>> getStudentPerformance(@PathVariable String studentId) {
        logger.debug("Getting performance report for student: {}", studentId);

        Map<String, Object> performance = reportingService.getStudentPerformance(studentId);

        return ResponseEntity.ok(performance);
    }

    /**
     * Get student progress over time
     * GET /api/reports/student/{studentId}/progress?gameId={gameId}&days={days}
     */
    @GetMapping("/student/{studentId}/progress")
    public ResponseEntity<Map<String, Object>> getStudentProgress(
            @PathVariable String studentId,
            @RequestParam(required = false) String gameId,
            @RequestParam(defaultValue = "30") int days) {

        logger.debug("Getting progress for student: {}, game: {}, days: {}", studentId, gameId, days);

        Map<String, Object> progress = reportingService.getStudentProgress(studentId, gameId, days);

        return ResponseEntity.ok(progress);
    }

    /**
     * Get class performance summary
     * POST /api/reports/class/performance
     * Body: { "studentIds": ["STU001", "STU002"], "gameId": "math-sprint" }
     */
    @PostMapping("/class/performance")
    public ResponseEntity<Map<String, Object>> getClassPerformance(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<String> studentIds = (List<String>) request.get("studentIds");
        String gameId = (String) request.get("gameId");

        logger.debug("Getting class performance for {} students", studentIds.size());

        Map<String, Object> performance = reportingService.getClassPerformance(studentIds, gameId);

        return ResponseEntity.ok(performance);
    }

    /**
     * Get game statistics
     * GET /api/reports/game/{gameId}/statistics
     */
    @GetMapping("/game/{gameId}/statistics")
    public ResponseEntity<Map<String, Object>> getGameStatistics(@PathVariable String gameId) {
        logger.debug("Getting statistics for game: {}", gameId);

        Map<String, Object> statistics = reportingService.getGameStatistics(gameId);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Get leaderboard for a game
     * GET /api/reports/game/{gameId}/leaderboard?limit={limit}
     */
    @GetMapping("/game/{gameId}/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "10") int limit) {

        logger.debug("Getting leaderboard for game: {}, limit: {}", gameId, limit);

        List<Map<String, Object>> leaderboard = reportingService.getLeaderboard(gameId, limit);

        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Get multiple students' performance (for teacher dashboard)
     * GET /api/reports/students/performance?studentIds=STU001,STU002,STU003
     */
    @GetMapping("/students/performance")
    public ResponseEntity<Map<String, Map<String, Object>>> getMultipleStudentsPerformance(
            @RequestParam String studentIds) {

        List<String> ids = Arrays.asList(studentIds.split(","));
        logger.debug("Getting performance for {} students", ids.size());

        Map<String, Map<String, Object>> results = ids.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        reportingService::getStudentPerformance
                ));

        return ResponseEntity.ok(results);
    }
}
