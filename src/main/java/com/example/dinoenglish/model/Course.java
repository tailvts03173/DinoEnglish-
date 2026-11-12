package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseLevel level;

    @Column(nullable = false)
    private Integer durationHours = 20;

    @Column(nullable = false)
    private Integer displayOrder = 1;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentResource> documents = new ArrayList<>();

    public Course() {
    }

    public Course(Long id, String title, String slug, String description, String imageUrl,
                  CourseLevel level, Integer durationHours, Integer displayOrder, Boolean active,
                  LocalDateTime createdAt, List<Lesson> lessons, List<DocumentResource> documents) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.imageUrl = imageUrl;
        this.level = level;
        this.durationHours = durationHours != null ? durationHours : 20;
        this.displayOrder = displayOrder != null ? displayOrder : 1;
        this.active = active != null ? active : true;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lessons = lessons != null ? lessons : new ArrayList<>();
        this.documents = documents != null ? documents : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 1;
        if (durationHours == null) durationHours = 20;
    }

    // Builder
    public static CourseBuilder builder() {
        return new CourseBuilder();
    }

    public static class CourseBuilder {
        private Long id;
        private String title;
        private String slug;
        private String description;
        private String imageUrl;
        private CourseLevel level;
        private Integer durationHours = 20;
        private Integer displayOrder = 1;
        private Boolean active = true;
        private LocalDateTime createdAt;
        private List<Lesson> lessons = new ArrayList<>();
        private List<DocumentResource> documents = new ArrayList<>();

        public CourseBuilder id(Long id) { this.id = id; return this; }
        public CourseBuilder title(String title) { this.title = title; return this; }
        public CourseBuilder slug(String slug) { this.slug = slug; return this; }
        public CourseBuilder description(String description) { this.description = description; return this; }
        public CourseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public CourseBuilder level(CourseLevel level) { this.level = level; return this; }
        public CourseBuilder durationHours(Integer durationHours) { this.durationHours = durationHours; return this; }
        public CourseBuilder displayOrder(Integer displayOrder) { this.displayOrder = displayOrder; return this; }
        public CourseBuilder active(Boolean active) { this.active = active; return this; }
        public CourseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CourseBuilder lessons(List<Lesson> lessons) { this.lessons = lessons; return this; }
        public CourseBuilder documents(List<DocumentResource> documents) { this.documents = documents; return this; }

        public Course build() {
            return new Course(id, title, slug, description, imageUrl, level, durationHours, displayOrder, active, createdAt, lessons, documents);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public CourseLevel getLevel() { return level; }
    public void setLevel(CourseLevel level) { this.level = level; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

    public List<DocumentResource> getDocuments() { return documents; }
    public void setDocuments(List<DocumentResource> documents) { this.documents = documents; }
}
