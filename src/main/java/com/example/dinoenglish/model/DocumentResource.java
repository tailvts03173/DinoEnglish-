package com.example.dinoenglish.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/** Embedded trong Course.documents. fileType: PDF, AUDIO, EXERCISE, DOC. */
public class DocumentResource {

    private String id = UUID.randomUUID().toString();

    private String title;

    private String description;

    private String fileType = "PDF";

    private String fileUrl;

    private String fileSize;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    /** Không lưu trong DB: DocumentService gán khóa học chứa tài liệu này để giao diện hiển thị tên khóa học. */
    @Transient
    private Course course;

    public DocumentResource() {
    }

    public String getIconClass() {
        if ("PDF".equalsIgnoreCase(fileType)) return "fa-file-pdf text-danger";
        if ("AUDIO".equalsIgnoreCase(fileType)) return "fa-file-audio text-warning";
        if ("EXERCISE".equalsIgnoreCase(fileType)) return "fa-file-lines text-primary";
        return "fa-file-word text-info";
    }

    public static DocumentResourceBuilder builder() { return new DocumentResourceBuilder(); }

    public static class DocumentResourceBuilder {
        private final DocumentResource o = new DocumentResource();
        public DocumentResourceBuilder id(String v) { o.id = v; return this; }
        public DocumentResourceBuilder title(String v) { o.title = v; return this; }
        public DocumentResourceBuilder description(String v) { o.description = v; return this; }
        public DocumentResourceBuilder fileType(String v) { o.fileType = v; return this; }
        public DocumentResourceBuilder fileUrl(String v) { o.fileUrl = v; return this; }
        public DocumentResourceBuilder fileSize(String v) { o.fileSize = v; return this; }
        public DocumentResourceBuilder uploadedAt(LocalDateTime v) { o.uploadedAt = v; return this; }
        public DocumentResourceBuilder course(Course v) { o.course = v; return this; }
        public DocumentResource build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
