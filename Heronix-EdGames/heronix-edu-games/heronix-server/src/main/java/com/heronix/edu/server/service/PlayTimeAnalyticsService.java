package com.heronix.edu.server.service;

import com.heronix.edu.server.dto.ClassPlayTimeReport;
import com.heronix.edu.server.dto.StudentPlayTimeReport;
import com.heronix.edu.server.entity.DeviceEntity;
import com.heronix.edu.server.entity.GameEntity;
import com.heronix.edu.server.entity.StudentEntity;
import com.heronix.edu.server.repository.DeviceRepository;
import com.heronix.edu.server.repository.GameRepository;
import com.heronix.edu.server.repository.GameScoreRepository;
import com.heronix.edu.server.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating play time analytics and reports
 * Used by teachers to track student engagement and share with parents/administrators
 */
@Service
public class PlayTimeAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(PlayTimeAnalyticsService.class);

    private final GameScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final DeviceRepository deviceRepository;
    private final GameRepository gameRepository;

    @Autowired
    public PlayTimeAnalyticsService(
            GameScoreRepository scoreRepository,
            StudentRepository studentRepository,
            DeviceRepository deviceRepository,
            GameRepository gameRepository) {
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.deviceRepository = deviceRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Get play time report for a specific student
     */
    public StudentPlayTimeReport getStudentReport(String studentId) {
        logger.info("Generating play time report for student: {}", studentId);

        StudentEntity student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        StudentPlayTimeReport report = new StudentPlayTimeReport();
        report.setStudentId(studentId);
        report.setStudentName(student.getFirstName() + " " + student.getLastInitial() + ".");
        report.setGradeLevel(student.getGradeLevel());

        // Get total play time
        Long totalSeconds = scoreRepository.sumTimeSecondsByStudentId(studentId);
        report.setTotalPlayTimeMinutes(totalSeconds != null ? (int)(totalSeconds / 60) : 0);

        // Get session count
        long sessionCount = scoreRepository.countByStudentId(studentId);
        report.setTotalSessions((int) sessionCount);

        // Calculate average session time
        if (sessionCount > 0 && totalSeconds != null) {
            report.setAverageSessionMinutes((double) totalSeconds / 60 / sessionCount);
        }

        // Get first and last play dates
        LocalDateTime firstPlay = scoreRepository.findFirstPlayDateByStudentId(studentId);
        LocalDateTime lastPlay = scoreRepository.findLastPlayDateByStudentId(studentId);
        if (firstPlay != null) report.setFirstPlayDate(firstPlay.toLocalDate());
        if (lastPlay != null) report.setLastPlayDate(lastPlay.toLocalDate());

        // Get game breakdown
        List<StudentPlayTimeReport.GamePlaySummary> gameBreakdown = new ArrayList<>();
        List<Object[]> breakdown = scoreRepository.getGameBreakdownByStudentId(studentId);

        Set<String> uniqueGames = new HashSet<>();
        for (Object[] row : breakdown) {
            String gameId = (String) row[0];
            uniqueGames.add(gameId);

            StudentPlayTimeReport.GamePlaySummary summary = new StudentPlayTimeReport.GamePlaySummary();
            summary.setGameId(gameId);

            // Get game name
            gameRepository.findByGameId(gameId).ifPresent(g -> summary.setGameName(g.getName()));

            Long gameSeconds = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            summary.setPlayTimeMinutes((int)(gameSeconds / 60));
            summary.setSessionsPlayed(((Number) row[2]).intValue());
            summary.setAverageScore(row[3] != null ? ((Number) row[3]).doubleValue() : 0);
            summary.setHighScore(row[4] != null ? ((Number) row[4]).intValue() : 0);

            gameBreakdown.add(summary);
        }

        report.setGameBreakdown(gameBreakdown);
        report.setTotalGamesPlayed(uniqueGames.size());

        // Calculate overall average score
        if (!breakdown.isEmpty()) {
            double totalAvg = breakdown.stream()
                .filter(r -> r[3] != null)
                .mapToDouble(r -> ((Number) r[3]).doubleValue())
                .average()
                .orElse(0);
            report.setAverageScore(totalAvg);
        }

        return report;
    }

    /**
     * Get play time report for a student within a date range
     */
    public StudentPlayTimeReport getStudentReport(String studentId, LocalDate startDate, LocalDate endDate) {
        logger.info("Generating play time report for student {} from {} to {}", studentId, startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        StudentEntity student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        StudentPlayTimeReport report = new StudentPlayTimeReport();
        report.setStudentId(studentId);
        report.setStudentName(student.getFirstName() + " " + student.getLastInitial() + ".");
        report.setGradeLevel(student.getGradeLevel());

        // Get total play time in range
        Long totalSeconds = scoreRepository.sumTimeSecondsByStudentIdAndDateRange(studentId, start, end);
        report.setTotalPlayTimeMinutes(totalSeconds != null ? (int)(totalSeconds / 60) : 0);

        // Get sessions in range
        var sessions = scoreRepository.findByStudentIdAndDateRange(studentId, start, end);
        report.setTotalSessions(sessions.size());

        // Calculate average session time
        if (!sessions.isEmpty() && totalSeconds != null) {
            report.setAverageSessionMinutes((double) totalSeconds / 60 / sessions.size());
        }

        // Get game breakdown in range
        List<StudentPlayTimeReport.GamePlaySummary> gameBreakdown = new ArrayList<>();
        List<Object[]> breakdown = scoreRepository.getGameBreakdownByStudentIdAndDateRange(studentId, start, end);

        Set<String> uniqueGames = new HashSet<>();
        for (Object[] row : breakdown) {
            String gameId = (String) row[0];
            uniqueGames.add(gameId);

            StudentPlayTimeReport.GamePlaySummary summary = new StudentPlayTimeReport.GamePlaySummary();
            summary.setGameId(gameId);
            gameRepository.findByGameId(gameId).ifPresent(g -> summary.setGameName(g.getName()));

            Long gameSeconds = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            summary.setPlayTimeMinutes((int)(gameSeconds / 60));
            summary.setSessionsPlayed(((Number) row[2]).intValue());
            summary.setAverageScore(row[3] != null ? ((Number) row[3]).doubleValue() : 0);
            summary.setHighScore(row[4] != null ? ((Number) row[4]).intValue() : 0);

            gameBreakdown.add(summary);
        }

        report.setGameBreakdown(gameBreakdown);
        report.setTotalGamesPlayed(uniqueGames.size());

        return report;
    }

    /**
     * Get play time report for a device
     */
    public StudentPlayTimeReport getDeviceReport(String deviceId) {
        logger.info("Generating play time report for device: {}", deviceId);

        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        StudentPlayTimeReport report = new StudentPlayTimeReport();
        report.setDeviceId(deviceId);
        report.setStudentId(device.getStudentId());
        report.setStudentName(device.getDeviceName());

        // Get scores for this device
        var scores = scoreRepository.findByDeviceId(deviceId);

        long totalSeconds = scores.stream()
            .filter(s -> s.getTimeSeconds() != null)
            .mapToLong(s -> s.getTimeSeconds())
            .sum();

        report.setTotalPlayTimeMinutes((int)(totalSeconds / 60));
        report.setTotalSessions(scores.size());

        if (!scores.isEmpty()) {
            report.setAverageSessionMinutes((double) totalSeconds / 60 / scores.size());

            // Get date range
            LocalDateTime first = scores.stream()
                .map(s -> s.getPlayedAt())
                .min(LocalDateTime::compareTo)
                .orElse(null);
            LocalDateTime last = scores.stream()
                .map(s -> s.getPlayedAt())
                .max(LocalDateTime::compareTo)
                .orElse(null);

            if (first != null) report.setFirstPlayDate(first.toLocalDate());
            if (last != null) report.setLastPlayDate(last.toLocalDate());
        }

        return report;
    }

    /**
     * Get class-wide play time report (all students)
     */
    public ClassPlayTimeReport getClassReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating class play time report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        ClassPlayTimeReport report = new ClassPlayTimeReport();
        report.setReportStartDate(startDate);
        report.setReportEndDate(endDate);

        // Get active students in date range
        List<String> activeStudentIds = scoreRepository.findActiveStudentIds(start, end);
        report.setActiveStudents(activeStudentIds.size());

        // Get total students
        report.setTotalStudents((int) studentRepository.count());

        // Get individual student reports
        List<StudentPlayTimeReport> studentReports = activeStudentIds.stream()
            .map(id -> getStudentReport(id, startDate, endDate))
            .collect(Collectors.toList());

        report.setStudentReports(studentReports);

        // Calculate totals
        int totalMinutes = studentReports.stream()
            .mapToInt(StudentPlayTimeReport::getTotalPlayTimeMinutes)
            .sum();
        report.setTotalPlayTimeMinutes(totalMinutes);

        int totalSessions = studentReports.stream()
            .mapToInt(StudentPlayTimeReport::getTotalSessions)
            .sum();
        report.setTotalSessions(totalSessions);

        if (!activeStudentIds.isEmpty()) {
            report.setAveragePlayTimePerStudent((double) totalMinutes / activeStudentIds.size());
        }

        // Get game usage stats
        List<ClassPlayTimeReport.GameUsageSummary> gameUsage = new ArrayList<>();
        List<Object[]> gameStats = scoreRepository.getGameUsageStats(start, end);

        for (Object[] row : gameStats) {
            ClassPlayTimeReport.GameUsageSummary summary = new ClassPlayTimeReport.GameUsageSummary();
            String gameId = (String) row[0];
            summary.setGameId(gameId);

            gameRepository.findByGameId(gameId).ifPresent(g -> {
                summary.setGameName(g.getName());
                summary.setSubject(g.getSubject());
            });

            summary.setUniquePlayers(((Number) row[1]).intValue());
            Long gameSeconds = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            summary.setTotalPlayTimeMinutes((int)(gameSeconds / 60));
            summary.setTotalSessions(((Number) row[3]).intValue());
            summary.setAverageScore(row[4] != null ? ((Number) row[4]).doubleValue() : 0);

            gameUsage.add(summary);
        }

        report.setGameUsage(gameUsage);

        // Calculate average score
        if (!gameStats.isEmpty()) {
            double avgScore = gameStats.stream()
                .filter(r -> r[4] != null)
                .mapToDouble(r -> ((Number) r[4]).doubleValue())
                .average()
                .orElse(0);
            report.setAverageScorePercentage(avgScore);
        }

        return report;
    }

    /**
     * Generate CSV export of student play time data
     */
    public String exportToCSV(ClassPlayTimeReport report) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Student ID,Student Name,Grade,Total Play Time (min),Sessions,Avg Session (min),Avg Score %,First Play,Last Play\n");

        // Data rows
        for (StudentPlayTimeReport student : report.getStudentReports()) {
            csv.append(String.format("%s,%s,%s,%d,%d,%.1f,%.1f,%s,%s\n",
                student.getStudentId(),
                student.getStudentName(),
                student.getGradeLevel() != null ? student.getGradeLevel() : "",
                student.getTotalPlayTimeMinutes(),
                student.getTotalSessions(),
                student.getAverageSessionMinutes(),
                student.getAverageScore(),
                student.getFirstPlayDate() != null ? student.getFirstPlayDate().toString() : "",
                student.getLastPlayDate() != null ? student.getLastPlayDate().toString() : ""
            ));
        }

        return csv.toString();
    }

    /**
     * Generate summary text for sharing with parents
     */
    public String generateParentSummary(StudentPlayTimeReport report) {
        StringBuilder summary = new StringBuilder();

        summary.append("=== Heronix Educational Games - Student Activity Report ===\n\n");
        summary.append(String.format("Student: %s\n", report.getStudentName()));
        if (report.getGradeLevel() != null) {
            summary.append(String.format("Grade: %s\n", report.getGradeLevel()));
        }
        summary.append("\n");

        summary.append("--- Play Time Summary ---\n");
        int hours = report.getTotalPlayTimeMinutes() / 60;
        int mins = report.getTotalPlayTimeMinutes() % 60;
        summary.append(String.format("Total Time: %d hours %d minutes\n", hours, mins));
        summary.append(String.format("Total Sessions: %d\n", report.getTotalSessions()));
        summary.append(String.format("Average Session: %.1f minutes\n", report.getAverageSessionMinutes()));
        summary.append(String.format("Games Played: %d\n", report.getTotalGamesPlayed()));

        if (report.getFirstPlayDate() != null && report.getLastPlayDate() != null) {
            summary.append(String.format("Active Period: %s to %s\n",
                report.getFirstPlayDate(), report.getLastPlayDate()));
        }

        if (report.getGameBreakdown() != null && !report.getGameBreakdown().isEmpty()) {
            summary.append("\n--- Games Breakdown ---\n");
            for (StudentPlayTimeReport.GamePlaySummary game : report.getGameBreakdown()) {
                summary.append(String.format("  %s: %d min, %d sessions, Avg Score: %.0f%%\n",
                    game.getGameName() != null ? game.getGameName() : game.getGameId(),
                    game.getPlayTimeMinutes(),
                    game.getSessionsPlayed(),
                    game.getAverageScore()
                ));
            }
        }

        summary.append("\n--- Generated by Heronix Educational Games Platform ---\n");

        return summary.toString();
    }
}
