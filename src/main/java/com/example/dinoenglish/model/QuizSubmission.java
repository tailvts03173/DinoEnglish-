package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

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
    private Integer score; // percentage (0 - 100)

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctCount;

    @Column(nullable = false)
    private Boolean passed = false;

    @Column(nullable = false)
    private Integer xpEarned = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    public QuizSubmission() {
    }

    public QuizSubmission(Long id, User user, Lesson lesson, Integer score, Integer totalQuestions,
                          Integer correctCount, Boolean passed, Integer xpEarned, LocalDateTime submittedAt) {
        this.id = id;
        this.user = user;
        this.lesson = lesson;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.passed = passed != null ? passed : false;
        this.xpEarned = xpEarned != null ? xpEarned : 0;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (passed == null) passed = false;
        if (xpEarned == null) xpEarned = 0;
    }

    // Builder
    public static QuizSubmissionBuilder builder() {
        return new QuizSubmissionBuilder();
    }

    public static class QuizSubmissionBuilder {
        private Long id;
        private User user;
        private Lesson lesson;
        private Integer score;
        private Integer totalQuestions;
        private Integer correctCount;
        private Boolean passed = false;
        private Integer xpEarned = 0;
        private LocalDateTime submittedAt;

        public QuizSubmissionBuilder id(Long id) { this.id = id; return this; }
        public QuizSubmissionBuilder user(User user) { this.user = user; return this; }
        public QuizSubmissionBuilder lesson(Lesson lesson) { this.lesson = lesson; return this; }
        public QuizSubmissionBuilder score(Integer score) { this.score = score; return this; }
        public QuizSubmissionBuilder totalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; return this; }
        public QuizSubmissionBuilder correctCount(Integer correctCount) { this.correctCount = correctCount; return this; }
        public QuizSubmissionBuilder passed(Boolean passed) { this.passed = passed; return this; }
        public QuizSubmissionBuilder xpEarned(Integer xpEarned) { this.xpEarned = xpEarned; return this; }
        public QuizSubmissionBuilder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }

        public QuizSubmission build() {
            return new QuizSubmission(id, user, lesson, score, totalQuestions, correctCount, passed, xpEarned, submittedAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public Integer getXpEarned() { return xpEarned; }
    public void setXpEarned(Integer xpEarned) { this.xpEarned = xpEarned; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
