package com.heronix.edu.server.repository;

import com.heronix.edu.server.entity.QuestionSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for question set persistence.
 */
@Repository
public interface QuestionSetRepository extends JpaRepository<QuestionSetEntity, String> {

    /**
     * Find question sets created by a teacher.
     */
    List<QuestionSetEntity> findByCreatedByOrderByUpdatedAtDesc(String teacherId);

    /**
     * Find public question sets.
     */
    List<QuestionSetEntity> findByIsPublicTrueOrderByNameAsc();

    /**
     * Find question sets by subject.
     */
    List<QuestionSetEntity> findBySubjectOrderByNameAsc(String subject);

    /**
     * Find question sets by grade level.
     */
    List<QuestionSetEntity> findByGradeLevelOrderByNameAsc(String gradeLevel);

    /**
     * Find question sets available to a teacher (their own + public).
     */
    @Query("SELECT qs FROM QuestionSetEntity qs WHERE qs.createdBy = :teacherId OR qs.isPublic = true ORDER BY qs.name ASC")
    List<QuestionSetEntity> findAvailableForTeacher(@Param("teacherId") String teacherId);

    /**
     * Search question sets by name or subject.
     */
    @Query("SELECT qs FROM QuestionSetEntity qs WHERE " +
           "(qs.createdBy = :teacherId OR qs.isPublic = true) AND " +
           "(LOWER(qs.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(qs.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<QuestionSetEntity> searchForTeacher(@Param("teacherId") String teacherId,
                                              @Param("searchTerm") String searchTerm);

    /**
     * Count questions in a set.
     */
    @Query("SELECT COUNT(q) FROM QuestionEntity q WHERE q.questionSet.setId = :setId")
    long countQuestions(@Param("setId") String setId);
}
