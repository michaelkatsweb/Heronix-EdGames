package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.GameBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for GameBundle entities
 */
@Repository
public interface GameBundleRepository extends JpaRepository<GameBundleEntity, String> {

    /**
     * Find all active bundles
     */
    List<GameBundleEntity> findByActiveTrue();

    /**
     * Find bundles by type (STANDARD or PREMIUM)
     */
    List<GameBundleEntity> findByBundleTypeAndActiveTrue(String bundleType);

    /**
     * Find bundles that contain a specific game
     */
    @Query("SELECT b FROM GameBundleEntity b JOIN b.gameIds g WHERE g = :gameId AND b.active = true")
    List<GameBundleEntity> findBundlesContainingGame(String gameId);

    /**
     * Find bundles by subject focus
     */
    List<GameBundleEntity> findBySubjectFocusAndActiveTrue(String subjectFocus);
}
