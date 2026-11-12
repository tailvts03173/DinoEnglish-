package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.DocumentResource;
import com.example.dinoenglish.repository.DocumentResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentResourceRepository documentResourceRepository;

    public DocumentService(DocumentResourceRepository documentResourceRepository) {
        this.documentResourceRepository = documentResourceRepository;
    }

    public List<DocumentResource> getAllDocuments() {
        return documentResourceRepository.findByOrderByUploadedAtDesc();
    }

    public List<DocumentResource> getDocumentsByCourse(Course course) {
        return documentResourceRepository.findByCourseOrderByUploadedAtDesc(course);
    }

    public Optional<DocumentResource> getDocumentById(Long id) {
        return documentResourceRepository.findById(id);
    }

    @Transactional
    public DocumentResource saveDocument(DocumentResource document) {
        return documentResourceRepository.save(document);
    }

    @Transactional
    public void deleteDocument(Long id) {
        documentResourceRepository.deleteById(id);
    }

    public long countTotalDocuments() {
        return documentResourceRepository.count();
    }
}
