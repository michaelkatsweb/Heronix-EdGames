package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Class entities.
 * Provides database access methods for class management.
 */
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findByTeacherId(Long teacherId);

    List<ClassEntity> findBySchoolId(String schoolId);

    List<ClassEntity> findByActiveTrue();

    List<ClassEntity> findByTeacherIdAndActiveTrue(Long teacherId);
}
