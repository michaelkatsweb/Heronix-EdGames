package com.heronix.edu.server.service;

import com.heronix.edu.common.model.Student;
import com.heronix.edu.server.entity.StudentEntity;
import com.heronix.edu.server.exception.ResourceNotFoundException;
import com.heronix.edu.server.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for student data management.
 * Implements FERPA-compliant student data access.
 */
@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Get student by ID
     */
    @Transactional(readOnly = true)
    public Student getStudent(String studentId) {
        logger.debug("Fetching student: {}", studentId);

        StudentEntity entity = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        auditService.logStudentDataAccess("SYSTEM", studentId, "READ");

        return entity.toModel();
    }

    /**
     * Check if student can participate (active, consented, not opted out)
     */
    @Transactional(readOnly = true)
    public boolean canParticipate(String studentId) {
        logger.debug("Checking if student can participate: {}", studentId);

        Student student = getStudent(studentId);
        return student.canParticipate();
    }

    /**
     * Get all active students with consent
     */
    @Transactional(readOnly = true)
    public List<Student> getEligibleStudents() {
        logger.debug("Fetching eligible students");

        return studentRepository.findByConsentGivenTrueAndOptedOutFalseAndActiveTrue()
                .stream()
                .map(StudentEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Create or update student
     */
    @Transactional
    public Student saveStudent(Student student) {
        logger.info("Saving student: {}", student.getStudentId());

        StudentEntity entity = StudentEntity.fromModel(student);
        StudentEntity saved = studentRepository.save(entity);

        auditService.logStudentDataAccess("SYSTEM", student.getStudentId(), "WRITE");

        return saved.toModel();
    }

    /**
     * Check if student exists
     */
    @Transactional(readOnly = true)
    public boolean studentExists(String studentId) {
        return studentRepository.findByStudentId(studentId).isPresent();
    }
}
