package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.QuizSubmission;
import com.example.dinoenglish.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByUserOrderBySubmittedAtDesc(User user);

    List<QuizSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    List<QuizSubmission> findByUserAndLesson(User user, Lesson lesson);

    List<QuizSubmission> findByUserIdAndLessonId(Long userId, Long lessonId);

    long countByUser(User user);
}
