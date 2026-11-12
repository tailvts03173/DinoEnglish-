package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserAndLesson(User user, Lesson lesson);

    Optional<UserProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    List<UserProgress> findByUser(User user);

    List<UserProgress> findByUserId(Long userId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.lesson.course.id = :courseId AND up.lesson.active = true AND up.completed = true")
    long countCompletedLessonsByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT up.completedAt FROM UserProgress up WHERE up.user.id = :userId ORDER BY up.completedAt DESC")
    List<LocalDateTime> findStudyActivityTimestamps(@Param("userId") Long userId);
}
