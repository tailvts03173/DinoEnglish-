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


@Document(collection = "quiz_submissions")
@CompoundIndex(name = "idx_user_lesson_time", def = "{'userId': 1, 'lessonId': 1, 'submittedAt': -1}")
public class QuizSubmission {

    @Id
    private String id;

    private String userId;

    private String lessonId;

    private Integer score;

    private Integer totalQuestions;

    private Integer correctCount;

    private Boolean passed = false;

    private Integer xpEarned = 0;

    private LocalDateTime submittedAt = LocalDateTime.now();

    /** Không lưu trong DB: service nạp từ lessonId để giao diện (Thymeleaf) dùng như trước. */
    @Transient
    private Lesson lesson;

    public QuizSubmission() {
    }

    public static QuizSubmissionBuilder builder() { return new QuizSubmissionBuilder(); }

    public static class QuizSubmissionBuilder {
        private final QuizSubmission o = new QuizSubmission();
        public QuizSubmissionBuilder id(String v) { o.id = v; return this; }
        public QuizSubmissionBuilder userId(String v) { o.userId = v; return this; }
        public QuizSubmissionBuilder lessonId(String v) { o.lessonId = v; return this; }
        public QuizSubmissionBuilder score(Integer v) { o.score = v; return this; }
        public QuizSubmissionBuilder totalQuestions(Integer v) { o.totalQuestions = v; return this; }
        public QuizSubmissionBuilder correctCount(Integer v) { o.correctCount = v; return this; }
        public QuizSubmissionBuilder passed(Boolean v) { o.passed = v; return this; }
        public QuizSubmissionBuilder xpEarned(Integer v) { o.xpEarned = v; return this; }
        public QuizSubmissionBuilder submittedAt(LocalDateTime v) { o.submittedAt = v; return this; }
        public QuizSubmissionBuilder lesson(Lesson v) { o.lesson = v; return this; }
        public QuizSubmission build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

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

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
}
