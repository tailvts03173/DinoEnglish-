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


@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;

    @Indexed(unique = true)
    private String slug;

    private String description;

    private String imageUrl;

    private CourseLevel level;

    private Integer durationHours = 20;

    private Integer displayOrder = 1;

    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private List<DocumentResource> documents = new ArrayList<>();

    /** Không lưu trong DB: lessons nằm ở collection "lessons" (tham chiếu bằng courseId). Service tự nạp khi cần hiển thị. */
    @Transient
    private List<Lesson> lessons = new ArrayList<>();

    public Course() {
    }

    public static CourseBuilder builder() { return new CourseBuilder(); }

    public static class CourseBuilder {
        private final Course o = new Course();
        public CourseBuilder id(String v) { o.id = v; return this; }
        public CourseBuilder title(String v) { o.title = v; return this; }
        public CourseBuilder slug(String v) { o.slug = v; return this; }
        public CourseBuilder description(String v) { o.description = v; return this; }
        public CourseBuilder imageUrl(String v) { o.imageUrl = v; return this; }
        public CourseBuilder level(CourseLevel v) { o.level = v; return this; }
        public CourseBuilder durationHours(Integer v) { o.durationHours = v; return this; }
        public CourseBuilder displayOrder(Integer v) { o.displayOrder = v; return this; }
        public CourseBuilder active(Boolean v) { o.active = v; return this; }
        public CourseBuilder createdAt(LocalDateTime v) { o.createdAt = v; return this; }
        public CourseBuilder documents(List<DocumentResource> v) { o.documents = v; return this; }
        public CourseBuilder lessons(List<Lesson> v) { o.lessons = v; return this; }
        public Course build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public List<DocumentResource> getDocuments() { return documents; }
    public void setDocuments(List<DocumentResource> documents) { this.documents = documents; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

}
