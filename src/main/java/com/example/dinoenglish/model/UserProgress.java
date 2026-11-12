package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Boolean completed = true;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    public UserProgress() {
    }

    public UserProgress(Long id, User user, Lesson lesson, Boolean completed, LocalDateTime completedAt) {
        this.id = id;
        this.user = user;
        this.lesson = lesson;
        this.completed = completed != null ? completed : true;
        this.completedAt = completedAt != null ? completedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
        if (completed == null) completed = true;
    }

    // Builder
    public static UserProgressBuilder builder() {
        return new UserProgressBuilder();
    }

    public static class UserProgressBuilder {
        private Long id;
        private User user;
        private Lesson lesson;
        private Boolean completed = true;
        private LocalDateTime completedAt;

        public UserProgressBuilder id(Long id) { this.id = id; return this; }
        public UserProgressBuilder user(User user) { this.user = user; return this; }
        public UserProgressBuilder lesson(Lesson lesson) { this.lesson = lesson; return this; }
        public UserProgressBuilder completed(Boolean completed) { this.completed = completed; return this; }
        public UserProgressBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public UserProgress build() {
            return new UserProgress(id, user, lesson, completed, completedAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
