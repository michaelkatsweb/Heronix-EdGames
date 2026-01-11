package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing GameEntity
 */
@Repository
public interface GameRepository extends JpaRepository<GameEntity, String> {

    /**
     * Find all active games
     */
    List<GameEntity> findByActiveTrue();

    /**
     * Find game by ID if active
     */
    Optional<GameEntity> findByGameIdAndActiveTrue(String gameId);

    /**
     * Find games by subject
     */
    List<GameEntity> findBySubjectAndActiveTrue(String subject);
}
