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
public interface LessonRepository extends MongoRepository<Lesson, String> {

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(String courseId);
    List<Lesson> findByCourseIdAndActiveTrueOrderByOrderIndexAsc(String courseId);
    long countByCourseId(String courseId);
    long countByCourseIdAndActiveTrue(String courseId);
    long countByActiveTrue();
    void deleteByCourseId(String courseId);

    /** Bản nhẹ (bỏ nội dung, từ vựng, câu hỏi) - dùng để đếm / liệt kê bài học trong danh sách khóa học. Không được save lại! */
    @Query(value = "{ 'courseId' : ?0 }", fields = "{ 'content' : 0, 'vocabularies' : 0, 'quizQuestions' : 0 }")
    List<Lesson> findLightByCourseId(String courseId);
}
