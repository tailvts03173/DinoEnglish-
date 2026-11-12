package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByLessonOrderByOrderIndexAsc(Lesson lesson);

    List<QuizQuestion> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    long countByLessonId(Long lessonId);
}
