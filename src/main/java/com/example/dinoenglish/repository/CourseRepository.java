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
public interface CourseRepository extends MongoRepository<Course, String> {

    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);
    long countByActiveTrue();
    List<Course> findByActiveTrueOrderByDisplayOrderAsc();
    List<Course> findByLevelAndActiveTrueOrderByDisplayOrderAsc(CourseLevel level);
    List<Course> findAllByOrderByDisplayOrderAsc();
}
