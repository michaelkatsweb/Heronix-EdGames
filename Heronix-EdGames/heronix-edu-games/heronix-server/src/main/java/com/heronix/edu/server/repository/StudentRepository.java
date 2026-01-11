package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Student entities.
 * Provides database access methods for student data.
 */
@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, String> {

    Optional<StudentEntity> findByStudentId(String studentId);

    List<StudentEntity> findBySchoolId(String schoolId);

    List<StudentEntity> findByActiveTrue();

    List<StudentEntity> findByConsentGivenTrueAndOptedOutFalseAndActiveTrue();
}
