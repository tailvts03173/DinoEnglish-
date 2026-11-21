package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.DocumentResource;
import com.example.dinoenglish.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/** Tài liệu được lưu nhúng trong Course.documents, nên mọi thao tác đều đi qua CourseRepository. */
@Service
public class DocumentService {

    private final CourseRepository courseRepository;

    public DocumentService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    private static final Comparator<DocumentResource> NEWEST_FIRST = Comparator.comparing(
            DocumentResource::getUploadedAt, Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));

    public List<DocumentResource> getAllDocuments() {
        List<DocumentResource> all = new ArrayList<>();
        for (Course c : courseRepository.findAll()) {
            if (c.getDocuments() == null) continue;
            for (DocumentResource d : c.getDocuments()) {
                d.setCourse(c);
                all.add(d);
            }
        }
        all.sort(NEWEST_FIRST);
        return all;
    }

    public List<DocumentResource> getDocumentsByCourse(Course course) {
        List<DocumentResource> list = new ArrayList<>();
        if (course != null && course.getDocuments() != null) {
            for (DocumentResource d : course.getDocuments()) {
                d.setCourse(course);
                list.add(d);
            }
        }
        list.sort(NEWEST_FIRST);
        return list;
    }

    public Optional<DocumentResource> getDocumentById(String id) {
        return getAllDocuments().stream().filter(d -> Objects.equals(d.getId(), id)).findFirst();
    }

    /** Thêm tài liệu vào khóa học. */
    public DocumentResource addDocument(String courseId, DocumentResource doc) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học!"));
        if (doc.getId() == null || doc.getId().isBlank()) doc.setId(UUID.randomUUID().toString());
        if (doc.getUploadedAt() == null) doc.setUploadedAt(LocalDateTime.now());
        if (doc.getFileType() == null || doc.getFileType().isBlank()) doc.setFileType("PDF");
        if (course.getDocuments() == null) course.setDocuments(new ArrayList<>());
        course.getDocuments().add(doc);
        courseRepository.save(course);
        doc.setCourse(course);
        return doc;
    }

    /** Xóa tài liệu theo id (tìm trong tất cả khóa học). */
    public void deleteDocument(String id) {
        if (id == null || id.isBlank()) return;
        for (Course c : courseRepository.findAll()) {
            if (c.getDocuments() != null && c.getDocuments().removeIf(d -> id.equals(d.getId()))) {
                courseRepository.save(c);
                return;
            }
        }
    }

    public long countTotalDocuments() {
        long total = 0;
        for (Course c : courseRepository.findAll()) {
            if (c.getDocuments() != null) total += c.getDocuments().size();
        }
        return total;
    }
}
