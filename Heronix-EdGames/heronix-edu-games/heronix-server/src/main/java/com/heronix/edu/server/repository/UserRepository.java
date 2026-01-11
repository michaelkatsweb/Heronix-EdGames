package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entities.
 * Provides database access methods for teacher and admin users.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findBySchoolId(String schoolId);

    List<UserEntity> findByRole(UserEntity.UserRole role);

    List<UserEntity> findByActiveTrue();

    boolean existsByUsername(String username);
}
