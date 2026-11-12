package com.example.dinoenglish.model;

import jakarta.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 100)
    private String iconClass;

    @Column(nullable = false)
    private Integer xpBonus = 50;

    public Badge() {
    }

    public Badge(Long id, String code, String title, String description, String iconClass, Integer xpBonus) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.iconClass = iconClass;
        this.xpBonus = xpBonus != null ? xpBonus : 50;
    }

    // Builder
    public static BadgeBuilder builder() {
        return new BadgeBuilder();
    }

    public static class BadgeBuilder {
        private Long id;
        private String code;
        private String title;
        private String description;
        private String iconClass;
        private Integer xpBonus = 50;

        public BadgeBuilder id(Long id) { this.id = id; return this; }
        public BadgeBuilder code(String code) { this.code = code; return this; }
        public BadgeBuilder title(String title) { this.title = title; return this; }
        public BadgeBuilder description(String description) { this.description = description; return this; }
        public BadgeBuilder iconClass(String iconClass) { this.iconClass = iconClass; return this; }
        public BadgeBuilder xpBonus(Integer xpBonus) { this.xpBonus = xpBonus; return this; }

        public Badge build() {
            return new Badge(id, code, title, description, iconClass, xpBonus);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
