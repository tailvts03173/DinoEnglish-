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
import com.example.dinoenglish.util.LevelUtil;


@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String avatarUrl = "/images/dino-logo.png";

    private Role role = Role.USER;

    private UserStatus status = UserStatus.ACTIVE;

    private Integer totalXp = 0;

    private Integer currentLevel = 1;

    private Integer currentStreak = 0;

    private Integer maxStreak = 0;

    private LocalDate lastStudyDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    // Custom Gamification Helpers (mốc XP dùng chung với LevelUtil)
    public String getDinoRank() {
        int level = currentLevel != null ? currentLevel : LevelUtil.levelForXp(totalXp);
        if (level <= 1) return "Baby Dino 🥚";
        if (level == 2) return "Little Dino 🦖";
        if (level == 3) return "Explorer Dino 🎒";
        if (level == 4) return "Scholar Dino 🎓";
        return "Dino Master 👑";
    }

    /** Tổng XP cần để lên cấp kế tiếp (mốc tuyệt đối). */
    public int getXpForNextLevel() {
        return LevelUtil.nextLevelThreshold(totalXp);
    }

    /** Phần trăm tiến trình trong cấp hiện tại (0-100). */
    public int getLevelProgressPercent() {
        return LevelUtil.progressPercent(totalXp);
    }

    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private final User o = new User();
        public UserBuilder id(String v) { o.id = v; return this; }
        public UserBuilder username(String v) { o.username = v; return this; }
        public UserBuilder password(String v) { o.password = v; return this; }
        public UserBuilder fullName(String v) { o.fullName = v; return this; }
        public UserBuilder email(String v) { o.email = v; return this; }
        public UserBuilder phone(String v) { o.phone = v; return this; }
        public UserBuilder avatarUrl(String v) { o.avatarUrl = v; return this; }
        public UserBuilder role(Role v) { o.role = v; return this; }
        public UserBuilder status(UserStatus v) { o.status = v; return this; }
        public UserBuilder totalXp(Integer v) { o.totalXp = v; return this; }
        public UserBuilder currentLevel(Integer v) { o.currentLevel = v; return this; }
        public UserBuilder currentStreak(Integer v) { o.currentStreak = v; return this; }
        public UserBuilder maxStreak(Integer v) { o.maxStreak = v; return this; }
        public UserBuilder lastStudyDate(LocalDate v) { o.lastStudyDate = v; return this; }
        public UserBuilder createdAt(LocalDateTime v) { o.createdAt = v; return this; }
        public User build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Integer getTotalXp() { return totalXp; }
    public void setTotalXp(Integer totalXp) { this.totalXp = totalXp; }

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getMaxStreak() { return maxStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }

    public LocalDate getLastStudyDate() { return lastStudyDate; }
    public void setLastStudyDate(LocalDate lastStudyDate) { this.lastStudyDate = lastStudyDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
