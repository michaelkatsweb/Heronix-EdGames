package com.heronix.edu.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a student in the Heronix platform.
 * Designed with FERPA compliance in mind - minimal PII collected.
 */
public class Student {
    
    /**
     * Internal student ID (NOT SSN or other external identifier)
     */
    private String studentId;
    
    /**
     * First name of student
     */
    private String firstName;
    
    /**
     * Last initial only (for privacy)
     */
    private String lastInitial;
    
    /**
     * Grade level (e.g., "3", "4", "5")
     */
    private String gradeLevel;
    
    /**
     * School identifier
     */
    private String schoolId;
    
    /**
     * Active status
     */
    private boolean active;
    
    /**
     * Consent status for data collection
     */
    private boolean consentGiven;
    
    /**
     * Date consent was obtained
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate consentDate;
    
    /**
     * Opt-out status (student/parent opted out of data collection)
     */
    private boolean optedOut;
    
    /**
     * Date student opted out
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate optOutDate;
    
    /**
     * Account creation date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
    // Constructors
    public Student() {
        this.active = true;
        this.consentGiven = false;
        this.optedOut = false;
        this.createdDate = LocalDate.now();
    }
    
    public Student(String studentId, String firstName, String lastInitial, String gradeLevel) {
        this();
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastInitial = lastInitial;
        this.gradeLevel = gradeLevel;
    }
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastInitial() {
        return lastInitial;
    }
    
    public void setLastInitial(String lastInitial) {
        this.lastInitial = lastInitial;
    }
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public String getSchoolId() {
        return schoolId;
    }
    
    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isConsentGiven() {
        return consentGiven;
    }
    
    public void setConsentGiven(boolean consentGiven) {
        this.consentGiven = consentGiven;
        if (consentGiven && this.consentDate == null) {
            this.consentDate = LocalDate.now();
        }
    }
    
    public LocalDate getConsentDate() {
        return consentDate;
    }
    
    public void setConsentDate(LocalDate consentDate) {
        this.consentDate = consentDate;
    }
    
    public boolean isOptedOut() {
        return optedOut;
    }
    
    public void setOptedOut(boolean optedOut) {
        this.optedOut = optedOut;
        if (optedOut && this.optOutDate == null) {
            this.optOutDate = LocalDate.now();
        }
    }
    
    public LocalDate getOptOutDate() {
        return optOutDate;
    }
    
    public void setOptOutDate(LocalDate optOutDate) {
        this.optOutDate = optOutDate;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    /**
     * Returns display name (first name + last initial)
     */
    public String getDisplayName() {
        return firstName + " " + lastInitial + ".";
    }
    
    /**
     * Check if student can participate (active, consented, not opted out)
     */
    public boolean canParticipate() {
        return active && consentGiven && !optedOut;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", active=" + active +
                ", consentGiven=" + consentGiven +
                '}';
    }
}
