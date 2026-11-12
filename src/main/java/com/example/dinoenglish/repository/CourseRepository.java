package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findBySlug(String slug);

    List<Course> findByActiveTrueOrderByDisplayOrderAsc();

    List<Course> findByLevelAndActiveTrueOrderByDisplayOrderAsc(CourseLevel level);

    boolean existsBySlug(String slug);

    long countByActiveTrue();
}
