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


/** courseId được lưu thêm (denormalize) để đếm tiến độ theo khóa học mà không cần join. */
@Document(collection = "user_progress")
@CompoundIndexes({
    @CompoundIndex(name = "uq_user_lesson", def = "{'userId': 1, 'lessonId': 1}", unique = true),
    @CompoundIndex(name = "idx_user_course", def = "{'userId': 1, 'courseId': 1}")
})
public class UserProgress {

    @Id
    private String id;

    private String userId;

    private String lessonId;

    private String courseId;

    private Boolean completed = true;

    private LocalDateTime completedAt = LocalDateTime.now();

    /** Không lưu trong DB: service nạp từ lessonId để giao diện (Thymeleaf) dùng như trước. */
    @Transient
    private Lesson lesson;

    public UserProgress() {
    }

    public static UserProgressBuilder builder() { return new UserProgressBuilder(); }

    public static class UserProgressBuilder {
        private final UserProgress o = new UserProgress();
        public UserProgressBuilder id(String v) { o.id = v; return this; }
        public UserProgressBuilder userId(String v) { o.userId = v; return this; }
        public UserProgressBuilder lessonId(String v) { o.lessonId = v; return this; }
        public UserProgressBuilder courseId(String v) { o.courseId = v; return this; }
        public UserProgressBuilder completed(Boolean v) { o.completed = v; return this; }
        public UserProgressBuilder completedAt(LocalDateTime v) { o.completedAt = v; return this; }
        public UserProgressBuilder lesson(Lesson v) { o.lesson = v; return this; }
        public UserProgress build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
}
