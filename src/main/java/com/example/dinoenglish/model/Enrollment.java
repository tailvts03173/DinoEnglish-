package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Double progressPercent = 0.0;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;

    public Enrollment() {
    }

    public Enrollment(Long id, User user, Course course, Double progressPercent, Boolean completed,
                      LocalDateTime enrolledAt, LocalDateTime completedAt) {
        this.id = id;
        this.user = user;
        this.course = course;
        this.progressPercent = progressPercent != null ? progressPercent : 0.0;
        this.completed = completed != null ? completed : false;
        this.enrolledAt = enrolledAt != null ? enrolledAt : LocalDateTime.now();
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
        if (progressPercent == null) progressPercent = 0.0;
        if (completed == null) completed = false;
    }

    // Builder
    public static EnrollmentBuilder builder() {
        return new EnrollmentBuilder();
    }

    public static class EnrollmentBuilder {
        private Long id;
        private User user;
        private Course course;
        private Double progressPercent = 0.0;
        private Boolean completed = false;
        private LocalDateTime enrolledAt;
        private LocalDateTime completedAt;

        public EnrollmentBuilder id(Long id) { this.id = id; return this; }
        public EnrollmentBuilder user(User user) { this.user = user; return this; }
        public EnrollmentBuilder course(Course course) { this.course = course; return this; }
        public EnrollmentBuilder progressPercent(Double progressPercent) { this.progressPercent = progressPercent; return this; }
        public EnrollmentBuilder completed(Boolean completed) { this.completed = completed; return this; }
        public EnrollmentBuilder enrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; return this; }
        public EnrollmentBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public Enrollment build() {
            return new Enrollment(id, user, course, progressPercent, completed, enrolledAt, completedAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
