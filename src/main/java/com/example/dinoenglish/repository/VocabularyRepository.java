package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByLesson(Lesson lesson);

    List<Vocabulary> findByLessonId(Long lessonId);
}
