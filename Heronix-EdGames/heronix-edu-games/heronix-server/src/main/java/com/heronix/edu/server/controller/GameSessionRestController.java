package com.heronix.edu.server.controller;

import com.heronix.edu.server.dto.game.*;
import com.heronix.edu.server.entity.QuestionSetEntity;
import com.heronix.edu.server.entity.QuestionEntity;
import com.heronix.edu.server.repository.QuestionSetRepository;
import com.heronix.edu.server.repository.QuestionRepository;
import com.heronix.edu.server.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST API endpoints for game session management.
 * Used by Teacher Portal for session creation and monitoring.
 */
@RestController
@RequestMapping("/api/game")
public class GameSessionRestController {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionRestController.class);

    private final GameSessionService gameSessionService;
    private final QuestionSetRepository questionSetRepository;
    private final QuestionRepository questionRepository;

    public GameSessionRestController(GameSessionService gameSessionService,
                                      QuestionSetRepository questionSetRepository,
                                      QuestionRepository questionRepository) {
        this.gameSessionService = gameSessionService;
        this.questionSetRepository = questionSetRepository;
        this.questionRepository = questionRepository;
    }

    // ========== Session Management ==========

    /**
     * Create a new game session.
     */
    @PostMapping("/sessions")
    public ResponseEntity<GameSessionDto> createSession(@RequestBody CreateSessionRequest request,
                                                         Principal principal) {
        String teacherId = principal != null ? principal.getName() : "teacher-1";
        GameSessionDto session = gameSessionService.createSession(request, teacherId);
        return ResponseEntity.ok(session);
    }

    /**
     * Get session details.
     */
    @GetMapping("/sessions/{sessionCode}")
    public ResponseEntity<GameSessionDto> getSession(@PathVariable String sessionCode) {
        GameSessionDto session = gameSessionService.getSession(sessionCode);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * Get teacher's sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<GameSessionDto>> getTeacherSessions(Principal principal) {
        String teacherId = principal != null ? principal.getName() : "teacher-1";
        List<GameSessionDto> sessions = gameSessionService.getTeacherSessions(teacherId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get session leaderboard.
     */
    @GetMapping("/sessions/{sessionCode}/leaderboard")
    public ResponseEntity<List<PlayerDto>> getLeaderboard(@PathVariable String sessionCode) {
        List<PlayerDto> leaderboard = gameSessionService.getLeaderboard(sessionCode);
        return ResponseEntity.ok(leaderboard);
    }

    // ========== Question Set Management ==========

    /**
     * Create a new question set.
     */
    @PostMapping("/question-sets")
    public ResponseEntity<QuestionSetDto> createQuestionSet(@RequestBody CreateQuestionSetRequest request,
                                                             Principal principal) {
        String teacherId = principal != null ? principal.getName() : "teacher-1";

        QuestionSetEntity entity = new QuestionSetEntity();
        entity.setSetId(UUID.randomUUID().toString());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setSubject(request.getSubject());
        entity.setGradeLevel(request.getGradeLevel());
        entity.setCreatedBy(teacherId);
        entity.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);

        questionSetRepository.save(entity);

        return ResponseEntity.ok(toQuestionSetDto(entity));
    }

    /**
     * Get question sets available to teacher.
     */
    @GetMapping("/question-sets")
    public ResponseEntity<List<QuestionSetDto>> getQuestionSets(Principal principal) {
        String teacherId = principal != null ? principal.getName() : "teacher-1";
        List<QuestionSetEntity> sets = questionSetRepository.findAvailableForTeacher(teacherId);
        List<QuestionSetDto> dtos = sets.stream().map(this::toQuestionSetDto).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific question set with questions.
     */
    @GetMapping("/question-sets/{setId}")
    public ResponseEntity<QuestionSetDto> getQuestionSet(@PathVariable String setId) {
        return questionSetRepository.findById(setId)
            .map(entity -> {
                QuestionSetDto dto = toQuestionSetDto(entity);
                List<QuestionDto> questions = questionRepository.findByQuestionSetId(setId)
                    .stream()
                    .map(this::toQuestionDto)
                    .toList();
                dto.setQuestions(questions);
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add questions to a question set.
     */
    @PostMapping("/question-sets/{setId}/questions")
    public ResponseEntity<QuestionDto> addQuestion(@PathVariable String setId,
                                                    @RequestBody AddQuestionRequest request) {
        QuestionSetEntity set = questionSetRepository.findById(setId).orElse(null);
        if (set == null) {
            return ResponseEntity.notFound().build();
        }

        QuestionEntity question = new QuestionEntity();
        question.setQuestionId(UUID.randomUUID().toString());
        question.setQuestionSet(set);
        question.setQuestionText(request.getQuestionText());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setWrongAnswer1(request.getWrongAnswer1());
        question.setWrongAnswer2(request.getWrongAnswer2());
        question.setWrongAnswer3(request.getWrongAnswer3());
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : 1);
        question.setImageUrl(request.getImageUrl());
        question.setExplanation(request.getExplanation());
        question.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);

        questionRepository.save(question);

        return ResponseEntity.ok(toQuestionDto(question));
    }

    /**
     * Bulk add questions to a question set.
     */
    @PostMapping("/question-sets/{setId}/questions/bulk")
    public ResponseEntity<List<QuestionDto>> addQuestionsBulk(@PathVariable String setId,
                                                               @RequestBody List<AddQuestionRequest> requests) {
        QuestionSetEntity set = questionSetRepository.findById(setId).orElse(null);
        if (set == null) {
            return ResponseEntity.notFound().build();
        }

        List<QuestionDto> addedQuestions = requests.stream()
            .map(request -> {
                QuestionEntity question = new QuestionEntity();
                question.setQuestionId(UUID.randomUUID().toString());
                question.setQuestionSet(set);
                question.setQuestionText(request.getQuestionText());
                question.setCorrectAnswer(request.getCorrectAnswer());
                question.setWrongAnswer1(request.getWrongAnswer1());
                question.setWrongAnswer2(request.getWrongAnswer2());
                question.setWrongAnswer3(request.getWrongAnswer3());
                question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : 1);
                question.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);

                questionRepository.save(question);
                return toQuestionDto(question);
            })
            .toList();

        return ResponseEntity.ok(addedQuestions);
    }

    /**
     * Update a question.
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable String questionId,
                                                       @RequestBody AddQuestionRequest request) {
        QuestionEntity question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return ResponseEntity.notFound().build();
        }

        question.setQuestionText(request.getQuestionText());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setWrongAnswer1(request.getWrongAnswer1());
        question.setWrongAnswer2(request.getWrongAnswer2());
        question.setWrongAnswer3(request.getWrongAnswer3());
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        question.setImageUrl(request.getImageUrl());
        question.setExplanation(request.getExplanation());
        if (request.getOrderIndex() != null) {
            question.setOrderIndex(request.getOrderIndex());
        }

        questionRepository.save(question);

        return ResponseEntity.ok(toQuestionDtoFull(question));
    }

    /**
     * Delete a question.
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String questionId) {
        questionRepository.deleteById(questionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update a question set.
     */
    @PutMapping("/question-sets/{setId}")
    public ResponseEntity<QuestionSetDto> updateQuestionSet(@PathVariable String setId,
                                                             @RequestBody CreateQuestionSetRequest request) {
        QuestionSetEntity set = questionSetRepository.findById(setId).orElse(null);
        if (set == null) {
            return ResponseEntity.notFound().build();
        }

        set.setName(request.getName());
        set.setDescription(request.getDescription());
        set.setSubject(request.getSubject());
        set.setGradeLevel(request.getGradeLevel());
        if (request.getIsPublic() != null) {
            set.setIsPublic(request.getIsPublic());
        }
        set.setUpdatedAt(java.time.LocalDateTime.now());

        questionSetRepository.save(set);

        return ResponseEntity.ok(toQuestionSetDto(set));
    }

    /**
     * Delete a question set and all its questions.
     */
    @DeleteMapping("/question-sets/{setId}")
    public ResponseEntity<Void> deleteQuestionSet(@PathVariable String setId) {
        // First delete all questions
        questionRepository.deleteByQuestionSetSetId(setId);
        // Then delete the set
        questionSetRepository.deleteById(setId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get preset/public question sets.
     */
    @GetMapping("/question-sets/presets")
    public ResponseEntity<List<QuestionSetDto>> getPresetQuestionSets() {
        List<QuestionSetEntity> sets = questionSetRepository.findByIsPublicTrueOrderByNameAsc();
        List<QuestionSetDto> dtos = sets.stream().map(this::toQuestionSetDto).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Clone a preset question set for a teacher.
     */
    @PostMapping("/question-sets/{setId}/clone")
    public ResponseEntity<QuestionSetDto> cloneQuestionSet(@PathVariable String setId,
                                                            Principal principal) {
        String teacherId = principal != null ? principal.getName() : "teacher-1";

        QuestionSetEntity original = questionSetRepository.findById(setId).orElse(null);
        if (original == null) {
            return ResponseEntity.notFound().build();
        }

        // Create new set
        QuestionSetEntity cloned = new QuestionSetEntity();
        cloned.setSetId(UUID.randomUUID().toString());
        cloned.setName(original.getName() + " (Copy)");
        cloned.setDescription(original.getDescription());
        cloned.setSubject(original.getSubject());
        cloned.setGradeLevel(original.getGradeLevel());
        cloned.setCreatedBy(teacherId);
        cloned.setIsPublic(false);

        questionSetRepository.save(cloned);

        // Clone questions
        List<QuestionEntity> originalQuestions = questionRepository.findByQuestionSetId(setId);
        for (QuestionEntity q : originalQuestions) {
            QuestionEntity clonedQ = new QuestionEntity();
            clonedQ.setQuestionId(UUID.randomUUID().toString());
            clonedQ.setQuestionSet(cloned);
            clonedQ.setQuestionText(q.getQuestionText());
            clonedQ.setCorrectAnswer(q.getCorrectAnswer());
            clonedQ.setWrongAnswer1(q.getWrongAnswer1());
            clonedQ.setWrongAnswer2(q.getWrongAnswer2());
            clonedQ.setWrongAnswer3(q.getWrongAnswer3());
            clonedQ.setDifficulty(q.getDifficulty());
            clonedQ.setImageUrl(q.getImageUrl());
            clonedQ.setExplanation(q.getExplanation());
            clonedQ.setOrderIndex(q.getOrderIndex());
            questionRepository.save(clonedQ);
        }

        return ResponseEntity.ok(toQuestionSetDto(cloned));
    }

    // ========== DTO Conversion ==========

    private QuestionSetDto toQuestionSetDto(QuestionSetEntity entity) {
        QuestionSetDto dto = new QuestionSetDto();
        dto.setSetId(entity.getSetId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setSubject(entity.getSubject());
        dto.setGradeLevel(entity.getGradeLevel());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setIsPublic(entity.getIsPublic());
        dto.setQuestionCount(entity.getQuestionCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private QuestionDto toQuestionDto(QuestionEntity entity) {
        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(entity.getQuestionId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setDifficulty(entity.getDifficulty());
        dto.setImageUrl(entity.getImageUrl());
        // Note: Don't expose correct answer in list view
        return dto;
    }

    private QuestionDto toQuestionDtoFull(QuestionEntity entity) {
        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(entity.getQuestionId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setCorrectAnswer(entity.getCorrectAnswer());
        dto.setWrongAnswer1(entity.getWrongAnswer1());
        dto.setWrongAnswer2(entity.getWrongAnswer2());
        dto.setWrongAnswer3(entity.getWrongAnswer3());
        dto.setDifficulty(entity.getDifficulty());
        dto.setImageUrl(entity.getImageUrl());
        dto.setExplanation(entity.getExplanation());
        dto.setOrderIndex(entity.getOrderIndex());
        return dto;
    }

    // ========== Request DTOs ==========

    public static class CreateQuestionSetRequest {
        private String name;
        private String description;
        private String subject;
        private String gradeLevel;
        private Boolean isPublic;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    }

    public static class AddQuestionRequest {
        private String questionText;
        private String correctAnswer;
        private String wrongAnswer1;
        private String wrongAnswer2;
        private String wrongAnswer3;
        private Integer difficulty;
        private String imageUrl;
        private String explanation;
        private Integer orderIndex;

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getWrongAnswer1() { return wrongAnswer1; }
        public void setWrongAnswer1(String wrongAnswer1) { this.wrongAnswer1 = wrongAnswer1; }
        public String getWrongAnswer2() { return wrongAnswer2; }
        public void setWrongAnswer2(String wrongAnswer2) { this.wrongAnswer2 = wrongAnswer2; }
        public String getWrongAnswer3() { return wrongAnswer3; }
        public void setWrongAnswer3(String wrongAnswer3) { this.wrongAnswer3 = wrongAnswer3; }
        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    }
}
