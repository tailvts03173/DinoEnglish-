package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseAndActiveTrueOrderByOrderIndexAsc(Course course);

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    long countByCourseIdAndActiveTrue(Long courseId);

    long countByActiveTrue();
}
