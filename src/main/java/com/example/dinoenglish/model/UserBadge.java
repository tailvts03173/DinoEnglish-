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


@Document(collection = "user_badges")
@CompoundIndex(name = "uq_user_badge", def = "{'userId': 1, 'badgeId': 1}", unique = true)
public class UserBadge {

    @Id
    private String id;

    private String userId;

    private String badgeId;

    private LocalDateTime earnedAt = LocalDateTime.now();

    /** Không lưu trong DB: service nạp từ badgeId để giao diện (Thymeleaf) dùng như trước. */
    @Transient
    private Badge badge;

    public UserBadge() {
    }

    public static UserBadgeBuilder builder() { return new UserBadgeBuilder(); }

    public static class UserBadgeBuilder {
        private final UserBadge o = new UserBadge();
        public UserBadgeBuilder id(String v) { o.id = v; return this; }
        public UserBadgeBuilder userId(String v) { o.userId = v; return this; }
        public UserBadgeBuilder badgeId(String v) { o.badgeId = v; return this; }
        public UserBadgeBuilder earnedAt(LocalDateTime v) { o.earnedAt = v; return this; }
        public UserBadgeBuilder badge(Badge v) { o.badge = v; return this; }
        public UserBadge build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }
}
