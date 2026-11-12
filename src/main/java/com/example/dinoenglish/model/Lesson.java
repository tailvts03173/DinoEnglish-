package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LessonType lessonType;

    @Column(nullable = false)
    private Integer orderIndex = 1;

    @Column(nullable = false)
    private Integer xpReward = 20;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(length = 255)
    private String audioUrl;

    @Column(length = 255)
    private String videoUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vocabulary> vocabularies = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    public Lesson() {
    }

    public Lesson(Long id, Course course, String title, String description, LessonType lessonType,
                  Integer orderIndex, Integer xpReward, String content, String audioUrl, String videoUrl,
                  Boolean active, List<Vocabulary> vocabularies, List<QuizQuestion> quizQuestions) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.description = description;
        this.lessonType = lessonType != null ? lessonType : LessonType.VOCABULARY;
        this.orderIndex = orderIndex != null ? orderIndex : 1;
        this.xpReward = xpReward != null ? xpReward : 20;
        this.content = content;
        this.audioUrl = audioUrl;
        this.videoUrl = videoUrl;
        this.active = active != null ? active : true;
        this.vocabularies = vocabularies != null ? vocabularies : new ArrayList<>();
        this.quizQuestions = quizQuestions != null ? quizQuestions : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (active == null) active = true;
        if (xpReward == null) xpReward = 20;
        if (orderIndex == null) orderIndex = 1;
        if (lessonType == null) lessonType = LessonType.VOCABULARY;
    }

    // Builder
    public static LessonBuilder builder() {
        return new LessonBuilder();
    }

    public static class LessonBuilder {
        private Long id;
        private Course course;
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

        public LessonBuilder id(Long id) { this.id = id; return this; }
        public LessonBuilder course(Course course) { this.course = course; return this; }
        public LessonBuilder title(String title) { this.title = title; return this; }
        public LessonBuilder description(String description) { this.description = description; return this; }
        public LessonBuilder lessonType(LessonType lessonType) { this.lessonType = lessonType; return this; }
        public LessonBuilder orderIndex(Integer orderIndex) { this.orderIndex = orderIndex; return this; }
        public LessonBuilder xpReward(Integer xpReward) { this.xpReward = xpReward; return this; }
        public LessonBuilder content(String content) { this.content = content; return this; }
        public LessonBuilder audioUrl(String audioUrl) { this.audioUrl = audioUrl; return this; }
        public LessonBuilder videoUrl(String videoUrl) { this.videoUrl = videoUrl; return this; }
        public LessonBuilder active(Boolean active) { this.active = active; return this; }
        public LessonBuilder vocabularies(List<Vocabulary> vocabularies) { this.vocabularies = vocabularies; return this; }
        public LessonBuilder quizQuestions(List<QuizQuestion> quizQuestions) { this.quizQuestions = quizQuestions; return this; }

        public Lesson build() {
            return new Lesson(id, course, title, description, lessonType, orderIndex, xpReward, content, audioUrl, videoUrl, active, vocabularies, quizQuestions);
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
}
