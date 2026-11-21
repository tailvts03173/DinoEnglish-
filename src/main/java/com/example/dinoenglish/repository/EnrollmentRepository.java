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
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    List<Enrollment> findByUserId(String userId);
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(String userId);
    List<Enrollment> findByCourseId(String courseId);
    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    long countByCourseId(String courseId);
    long countByUserIdAndCompletedTrue(String userId);
    void deleteByCourseId(String courseId);
}
