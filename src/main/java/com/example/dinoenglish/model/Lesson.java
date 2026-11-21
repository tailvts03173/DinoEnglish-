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


@Document(collection = "lessons")
@CompoundIndex(name = "idx_course_order", def = "{'courseId': 1, 'orderIndex': 1}")
public class Lesson {

    @Id
    private String id;

    private String courseId;

    private String title;

    private String description;

    private LessonType lessonType = LessonType.VOCABULARY;

    private Integer orderIndex = 1;

    private Integer xpReward = 20;

    private String content;

    private String audioUrl;

    private String videoUrl;

    private Boolean active = true;

    private List<Vocabulary> vocabularies = new ArrayList<>();

    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    /** Không lưu trong DB: service nạp từ courseId để giao diện (Thymeleaf) dùng như trước. */
    @Transient
    private Course course;

    public Lesson() {
    }

    public static LessonBuilder builder() { return new LessonBuilder(); }

    public static class LessonBuilder {
        private final Lesson o = new Lesson();
        public LessonBuilder id(String v) { o.id = v; return this; }
        public LessonBuilder courseId(String v) { o.courseId = v; return this; }
        public LessonBuilder title(String v) { o.title = v; return this; }
        public LessonBuilder description(String v) { o.description = v; return this; }
        public LessonBuilder lessonType(LessonType v) { o.lessonType = v; return this; }
        public LessonBuilder orderIndex(Integer v) { o.orderIndex = v; return this; }
        public LessonBuilder xpReward(Integer v) { o.xpReward = v; return this; }
        public LessonBuilder content(String v) { o.content = v; return this; }
        public LessonBuilder audioUrl(String v) { o.audioUrl = v; return this; }
        public LessonBuilder videoUrl(String v) { o.videoUrl = v; return this; }
        public LessonBuilder active(Boolean v) { o.active = v; return this; }
        public LessonBuilder vocabularies(List<Vocabulary> v) { o.vocabularies = v; return this; }
        public LessonBuilder quizQuestions(List<QuizQuestion> v) { o.quizQuestions = v; return this; }
        public LessonBuilder course(Course v) { o.course = v; return this; }
        public Lesson build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Integer getXpReward() { return xpReward; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<Vocabulary> getVocabularies() { return vocabularies; }
    public void setVocabularies(List<Vocabulary> vocabularies) { this.vocabularies = vocabularies; }

    public List<QuizQuestion> getQuizQuestions() { return quizQuestions; }
    public void setQuizQuestions(List<QuizQuestion> quizQuestions) { this.quizQuestions = quizQuestions; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
