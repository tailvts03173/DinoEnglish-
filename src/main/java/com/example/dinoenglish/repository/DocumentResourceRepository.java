package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.DocumentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentResourceRepository extends JpaRepository<DocumentResource, Long> {

    List<DocumentResource> findByOrderByUploadedAtDesc();

    List<DocumentResource> findByCourseOrderByUploadedAtDesc(Course course);

    List<DocumentResource> findByFileTypeIgnoreCase(String fileType);
}
