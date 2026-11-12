package com.example.dinoenglish.model;

import com.example.dinoenglish.util.LevelUtil;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String avatarUrl = "/images/dino-logo.png";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    private Integer totalXp = 0;

    @Column(nullable = false)
    private Integer currentLevel = 1;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(nullable = false)
    private Integer maxStreak = 0;

    private LocalDate lastStudyDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String username, String password, String fullName, String email, String phone,
                String avatarUrl, Role role, UserStatus status, Integer totalXp, Integer currentLevel,
                Integer currentStreak, Integer maxStreak, LocalDate lastStudyDate, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl != null ? avatarUrl : "/images/dino-logo.png";
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.totalXp = totalXp != null ? totalXp : 0;
        this.currentLevel = currentLevel != null ? currentLevel : 1;
        this.currentStreak = currentStreak != null ? currentStreak : 0;
        this.maxStreak = maxStreak != null ? maxStreak : 0;
        this.lastStudyDate = lastStudyDate;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (totalXp == null) totalXp = 0;
        if (currentLevel == null) currentLevel = 1;
        if (currentStreak == null) currentStreak = 0;
        if (maxStreak == null) maxStreak = 0;
        if (role == null) role = Role.USER;
        if (status == null) status = UserStatus.ACTIVE;
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarUrl = "/images/dino-logo.png";
        }
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

    // Builder Pattern
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String fullName;
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
        private LocalDateTime createdAt;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder status(UserStatus status) { this.status = status; return this; }
        public UserBuilder totalXp(Integer totalXp) { this.totalXp = totalXp; return this; }
        public UserBuilder currentLevel(Integer currentLevel) { this.currentLevel = currentLevel; return this; }
        public UserBuilder currentStreak(Integer currentStreak) { this.currentStreak = currentStreak; return this; }
        public UserBuilder maxStreak(Integer maxStreak) { this.maxStreak = maxStreak; return this; }
        public UserBuilder lastStudyDate(LocalDate lastStudyDate) { this.lastStudyDate = lastStudyDate; return this; }
        public UserBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            return new User(id, username, password, fullName, email, phone, avatarUrl, role, status,
                    totalXp, currentLevel, currentStreak, maxStreak, lastStudyDate, createdAt);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
