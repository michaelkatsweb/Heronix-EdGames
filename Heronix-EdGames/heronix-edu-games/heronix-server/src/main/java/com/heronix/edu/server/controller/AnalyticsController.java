package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.ClassPlayTimeReport;
import com.heronix.edu.server.dto.StudentPlayTimeReport;
import com.heronix.edu.server.service.PlayTimeAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for play time analytics
 * Used by teachers to track student engagement and share with parents/administrators
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final PlayTimeAnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(PlayTimeAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get play time report for a specific student (all time)
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentPlayTimeReport> getStudentReport(
            @PathVariable String studentId) {
        StudentPlayTimeReport report = analyticsService.getStudentReport(studentId);
        return ResponseEntity.ok(report);
    }

    /**
     * Get play time report for a specific student with date range
     */
    @GetMapping("/student/{studentId}/range")
    public ResponseEntity<StudentPlayTimeReport> getStudentReportWithRange(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StudentPlayTimeReport report = analyticsService.getStudentReport(studentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Get play time report for a specific device
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<StudentPlayTimeReport> getDeviceReport(
            @PathVariable String deviceId) {
        StudentPlayTimeReport report = analyticsService.getDeviceReport(deviceId);
        return ResponseEntity.ok(report);
    }

    /**
     * Get class-wide play time report for a date range
     */
    @GetMapping("/class")
    public ResponseEntity<ClassPlayTimeReport> getClassReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ClassPlayTimeReport report = analyticsService.getClassReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Export class report as CSV
     */
    @GetMapping("/class/export/csv")
    public ResponseEntity<String> exportClassReportCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ClassPlayTimeReport report = analyticsService.getClassReport(startDate, endDate);
        String csv = analyticsService.exportToCSV(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"play-time-report-" + startDate + "-to-" + endDate + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    /**
     * Get parent-friendly summary for a student
     */
    @GetMapping("/student/{studentId}/parent-summary")
    public ResponseEntity<String> getParentSummary(@PathVariable String studentId) {
        StudentPlayTimeReport report = analyticsService.getStudentReport(studentId);
        String summary = analyticsService.generateParentSummary(report);

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(summary);
    }

    /**
     * Get parent-friendly summary for a student with date range
     */
    @GetMapping("/student/{studentId}/parent-summary/range")
    public ResponseEntity<String> getParentSummaryWithRange(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StudentPlayTimeReport report = analyticsService.getStudentReport(studentId, startDate, endDate);
        String summary = analyticsService.generateParentSummary(report);

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(summary);
    }

    /**
     * Export student report for download (text format for sharing)
     */
    @GetMapping("/student/{studentId}/export")
    public ResponseEntity<String> exportStudentReport(@PathVariable String studentId) {
        StudentPlayTimeReport report = analyticsService.getStudentReport(studentId);
        String summary = analyticsService.generateParentSummary(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"student-report-" + studentId + ".txt\"")
            .contentType(MediaType.TEXT_PLAIN)
            .body(summary);
    }
}
