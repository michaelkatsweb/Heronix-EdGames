package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for question persistence.
 */
@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, String> {

    /**
     * Find all questions in a question set.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.questionSet.setId = :setId ORDER BY q.orderIndex ASC")
    List<QuestionEntity> findByQuestionSetId(@Param("setId") String setId);

    /**
     * Find questions by difficulty.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.questionSet.setId = :setId AND q.difficulty = :difficulty ORDER BY q.orderIndex ASC")
    List<QuestionEntity> findByQuestionSetIdAndDifficulty(@Param("setId") String setId,
                                                           @Param("difficulty") Integer difficulty);

    /**
     * Get random questions from a set.
     */
    @Query(value = "SELECT * FROM questions WHERE set_id = :setId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuestionEntity> findRandomQuestions(@Param("setId") String setId, @Param("limit") int limit);

    /**
     * Count questions by difficulty in a set.
     */
    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionEntity q WHERE q.questionSet.setId = :setId GROUP BY q.difficulty")
    List<Object[]> countByDifficulty(@Param("setId") String setId);

    /**
     * Delete all questions in a set.
     */
    void deleteByQuestionSetSetId(String setId);
}
