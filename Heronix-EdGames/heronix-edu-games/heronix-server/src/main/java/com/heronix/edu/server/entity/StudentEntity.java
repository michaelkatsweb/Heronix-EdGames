package com.heronix.edu.server.entity;

import com.heronix.edu.common.model.Student;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * JPA Entity for Student data.
 * Maps to the 'students' table in the database.
 * FERPA-compliant: stores only minimal PII (first name + last initial).
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_school_id", columnList = "school_id"),
    @Index(name = "idx_student_id", columnList = "student_id", unique = true)
})
public class StudentEntity {

    @Id
    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_initial", nullable = false, length = 1)
    private String lastInitial;

    @Column(name = "grade_level", length = 10)
    private String gradeLevel;

    @Column(name = "school_id", length = 50)
    private String schoolId;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven = false;

    @Column(name = "consent_date")
    private LocalDate consentDate;

    @Column(name = "opted_out", nullable = false)
    private Boolean optedOut = false;

    @Column(name = "opt_out_date")
    private LocalDate optOutDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    // Constructors
    public StudentEntity() {
    }

    public StudentEntity(String studentId, String firstName, String lastInitial, String gradeLevel) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastInitial = lastInitial;
        this.gradeLevel = gradeLevel;
        this.active = true;
        this.consentGiven = false;
        this.optedOut = false;
        this.createdDate = LocalDate.now();
    }

    /**
     * Convert Student domain model to StudentEntity
     */
    public static StudentEntity fromModel(Student student) {
        StudentEntity entity = new StudentEntity();
        entity.setStudentId(student.getStudentId());
        entity.setFirstName(student.getFirstName());
        entity.setLastInitial(student.getLastInitial());
        entity.setGradeLevel(student.getGradeLevel());
        entity.setSchoolId(student.getSchoolId());
        entity.setActive(student.isActive());
        entity.setConsentGiven(student.isConsentGiven());
        entity.setConsentDate(student.getConsentDate());
        entity.setOptedOut(student.isOptedOut());
        entity.setOptOutDate(student.getOptOutDate());
        entity.setCreatedDate(student.getCreatedDate());
        return entity;
    }

    /**
     * Convert StudentEntity to Student domain model
     */
    public Student toModel() {
        Student student = new Student();
        student.setStudentId(this.studentId);
        student.setFirstName(this.firstName);
        student.setLastInitial(this.lastInitial);
        student.setGradeLevel(this.gradeLevel);
        student.setSchoolId(this.schoolId);
        student.setActive(this.active);
        student.setConsentGiven(this.consentGiven);
        student.setConsentDate(this.consentDate);
        student.setOptedOut(this.optedOut);
        student.setOptOutDate(this.optOutDate);
        student.setCreatedDate(this.createdDate);
        return student;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    public LocalDate getConsentDate() {
        return consentDate;
    }

    public void setConsentDate(LocalDate consentDate) {
        this.consentDate = consentDate;
    }

    public Boolean getOptedOut() {
        return optedOut;
    }

    public void setOptedOut(Boolean optedOut) {
        this.optedOut = optedOut;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentEntity that = (StudentEntity) o;
        return Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return "StudentEntity{" +
                "studentId='" + studentId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastInitial='" + lastInitial + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", active=" + active +
                '}';
    }
}
