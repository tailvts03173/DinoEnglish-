package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends MongoRepository<QuizSubmission, String> {

    List<QuizSubmission> findByUserIdOrderBySubmittedAtDesc(String userId);
    List<QuizSubmission> findByUserIdAndLessonIdOrderBySubmittedAtDesc(String userId, String lessonId);
    Optional<QuizSubmission> findFirstByUserIdAndLessonIdOrderByScoreDesc(String userId, String lessonId);
    boolean existsByUserIdAndLessonIdAndPassedTrue(String userId, String lessonId);
    boolean existsByLessonId(String lessonId);
    long countByUserId(String userId);
    void deleteByLessonId(String lessonId);
}
