package com.example.dinoenglish.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "badge_id"})
})
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    public UserBadge() {
    }

    public UserBadge(Long id, User user, Badge badge, LocalDateTime earnedAt) {
        this.id = id;
        this.user = user;
        this.badge = badge;
        this.earnedAt = earnedAt != null ? earnedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) {
            earnedAt = LocalDateTime.now();
        }
    }

    // Builder
    public static UserBadgeBuilder builder() {
        return new UserBadgeBuilder();
    }

    public static class UserBadgeBuilder {
        private Long id;
        private User user;
        private Badge badge;
        private LocalDateTime earnedAt;

        public UserBadgeBuilder id(Long id) { this.id = id; return this; }
        public UserBadgeBuilder user(User user) { this.user = user; return this; }
        public UserBadgeBuilder badge(Badge badge) { this.badge = badge; return this; }
        public UserBadgeBuilder earnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; return this; }

        public UserBadge build() {
            return new UserBadge(id, user, badge, earnedAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
