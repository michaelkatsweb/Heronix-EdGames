package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.RegistrationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RegistrationCode entities.
 * Provides database access methods for registration codes.
 */
@Repository
public interface RegistrationCodeRepository extends JpaRepository<RegistrationCodeEntity, Long> {

    Optional<RegistrationCodeEntity> findByCode(String code);

    List<RegistrationCodeEntity> findByActiveTrue();

    List<RegistrationCodeEntity> findByTeacherId(String teacherId);

    List<RegistrationCodeEntity> findByClassId(String classId);
}
