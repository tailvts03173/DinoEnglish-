package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_resources")
public class DocumentResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String fileType = "PDF"; // PDF, AUDIO, EXERCISE, DOC

    @Column(nullable = false, length = 255)
    private String fileUrl;

    @Column(length = 50)
    private String fileSize;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public DocumentResource() {
    }

    public DocumentResource(Long id, Course course, String title, String description,
                            String fileType, String fileUrl, String fileSize, LocalDateTime uploadedAt) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.description = description;
        this.fileType = fileType != null ? fileType : "PDF";
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (fileType == null) fileType = "PDF";
    }

    public String getIconClass() {
        if ("PDF".equalsIgnoreCase(fileType)) return "fa-file-pdf text-danger";
        if ("AUDIO".equalsIgnoreCase(fileType)) return "fa-file-audio text-warning";
        if ("EXERCISE".equalsIgnoreCase(fileType)) return "fa-file-lines text-primary";
        return "fa-file-word text-info";
    }

    // Builder
    public static DocumentResourceBuilder builder() {
        return new DocumentResourceBuilder();
    }

    public static class DocumentResourceBuilder {
        private Long id;
        private Course course;
        private String title;
        private String description;
        private String fileType = "PDF";
        private String fileUrl;
        private String fileSize;
        private LocalDateTime uploadedAt;

        public DocumentResourceBuilder id(Long id) { this.id = id; return this; }
        public DocumentResourceBuilder course(Course course) { this.course = course; return this; }
        public DocumentResourceBuilder title(String title) { this.title = title; return this; }
        public DocumentResourceBuilder description(String description) { this.description = description; return this; }
        public DocumentResourceBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public DocumentResourceBuilder fileUrl(String fileUrl) { this.fileUrl = fileUrl; return this; }
        public DocumentResourceBuilder fileSize(String fileSize) { this.fileSize = fileSize; return this; }
        public DocumentResourceBuilder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public DocumentResource build() {
            return new DocumentResource(id, course, title, description, fileType, fileUrl, fileSize, uploadedAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
