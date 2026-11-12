package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.Enrollment;
import com.example.dinoenglish.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByUserAndCourse(User user, Course course);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findByUserOrderByEnrolledAtDesc(User user);

    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);

    long countByCourse(Course course);

    long countByUser(User user);
}
