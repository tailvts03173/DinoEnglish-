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


@Document(collection = "enrollments")
@CompoundIndex(name = "uq_user_course", def = "{'userId': 1, 'courseId': 1}", unique = true)
public class Enrollment {

    @Id
    private String id;

    private String userId;

    @Indexed
    private String courseId;

    private Double progressPercent = 0.0;

    private Boolean completed = false;

    private LocalDateTime enrolledAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    /** Không lưu trong DB: service nạp từ courseId để giao diện (Thymeleaf) dùng như trước. */
    @Transient
    private Course course;

    public Enrollment() {
    }

    public static EnrollmentBuilder builder() { return new EnrollmentBuilder(); }

    public static class EnrollmentBuilder {
        private final Enrollment o = new Enrollment();
        public EnrollmentBuilder id(String v) { o.id = v; return this; }
        public EnrollmentBuilder userId(String v) { o.userId = v; return this; }
        public EnrollmentBuilder courseId(String v) { o.courseId = v; return this; }
        public EnrollmentBuilder progressPercent(Double v) { o.progressPercent = v; return this; }
        public EnrollmentBuilder completed(Boolean v) { o.completed = v; return this; }
        public EnrollmentBuilder enrolledAt(LocalDateTime v) { o.enrolledAt = v; return this; }
        public EnrollmentBuilder completedAt(LocalDateTime v) { o.completedAt = v; return this; }
        public EnrollmentBuilder course(Course v) { o.course = v; return this; }
        public Enrollment build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
