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


@Document(collection = "badges")
public class Badge {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String title;

    private String description;

    private String iconClass;

    private Integer xpBonus = 50;

    public Badge() {
    }

    public static BadgeBuilder builder() { return new BadgeBuilder(); }

    public static class BadgeBuilder {
        private final Badge o = new Badge();
        public BadgeBuilder id(String v) { o.id = v; return this; }
        public BadgeBuilder code(String v) { o.code = v; return this; }
        public BadgeBuilder title(String v) { o.title = v; return this; }
        public BadgeBuilder description(String v) { o.description = v; return this; }
        public BadgeBuilder iconClass(String v) { o.iconClass = v; return this; }
        public BadgeBuilder xpBonus(Integer v) { o.xpBonus = v; return this; }
        public Badge build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public Integer getXpBonus() { return xpBonus; }
    public void setXpBonus(Integer xpBonus) { this.xpBonus = xpBonus; }

}
