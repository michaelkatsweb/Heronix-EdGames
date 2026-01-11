package com.heronix.edu.server.service;

import com.heronix.edu.common.model.GameScore;
import com.heronix.edu.server.entity.GameScoreEntity;
import com.heronix.edu.server.repository.GameScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for game score management.
 */
@Service
public class GameScoreService {

    private static final Logger logger = LoggerFactory.getLogger(GameScoreService.class);

    @Autowired
    private GameScoreRepository gameScoreRepository;

    /**
     * Save a single game score
     */
    @Transactional
    public GameScore saveScore(GameScore score) {
        logger.debug("Saving score: {}", score.getScoreId());

        GameScoreEntity entity = GameScoreEntity.fromModel(score);
        entity.setSynced(true);
        entity.setSyncedAt(LocalDateTime.now());

        GameScoreEntity saved = gameScoreRepository.save(entity);

        return saved.toModel();
    }

    /**
     * Save multiple game scores in a batch
     */
    @Transactional
    public List<GameScore> saveScores(List<GameScore> scores) {
        logger.info("Batch saving {} scores", scores.size());

        List<GameScoreEntity> entities = scores.stream()
                .map(score -> {
                    GameScoreEntity entity = GameScoreEntity.fromModel(score);
                    entity.setSynced(true);
                    entity.setSyncedAt(LocalDateTime.now());
                    return entity;
                })
                .collect(Collectors.toList());

        List<GameScoreEntity> saved = gameScoreRepository.saveAll(entities);

        return saved.stream()
                .map(GameScoreEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get score by ID (for duplicate detection)
     */
    @Transactional(readOnly = true)
    public Optional<GameScore> getScoreById(String scoreId) {
        return gameScoreRepository.findByScoreId(scoreId)
                .map(GameScoreEntity::toModel);
    }

    /**
     * Get all scores for a student
     */
    @Transactional(readOnly = true)
    public List<GameScore> getStudentScores(String studentId) {
        logger.debug("Fetching scores for student: {}", studentId);

        return gameScoreRepository.findByStudentId(studentId)
                .stream()
                .map(GameScoreEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get scores for a student and specific game
     */
    @Transactional(readOnly = true)
    public List<GameScore> getStudentGameScores(String studentId, String gameId) {
        logger.debug("Fetching scores for student: {} and game: {}", studentId, gameId);

        return gameScoreRepository.findByStudentIdAndGameId(studentId, gameId)
                .stream()
                .map(GameScoreEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Check if score already exists (for duplicate detection)
     */
    @Transactional(readOnly = true)
    public boolean scoreExists(String scoreId) {
        return gameScoreRepository.findByScoreId(scoreId).isPresent();
    }
}
