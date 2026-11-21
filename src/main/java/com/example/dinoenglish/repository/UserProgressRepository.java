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
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {

    List<UserProgress> findByUserId(String userId);
    List<UserProgress> findByUserIdAndCourseId(String userId, String courseId);
    Optional<UserProgress> findByUserIdAndLessonId(String userId, String lessonId);
    boolean existsByUserIdAndLessonId(String userId, String lessonId);
    boolean existsByLessonId(String lessonId);
    boolean existsByCourseId(String courseId);
    long countByUserId(String userId);
    long countByUserIdAndCourseId(String userId, String courseId);
    long countByUserIdAndCourseIdAndCompletedTrue(String userId, String courseId);
    void deleteByLessonId(String lessonId);
}
