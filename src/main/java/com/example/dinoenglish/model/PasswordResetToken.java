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


/** Mã xác minh 6 số gửi qua email để đặt lại mật khẩu. Chỉ lưu hash của mã, không lưu mã gốc. */
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String codeHash;

    /** TTL index: MongoDB tự xóa document khi quá hạn (kiểm tra mỗi ~60 giây). */
    @Indexed(expireAfter = "0s")
    private LocalDateTime expiresAt;

    private Integer attempts = 0;

    private Boolean used = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PasswordResetToken() {
    }

    public static PasswordResetTokenBuilder builder() { return new PasswordResetTokenBuilder(); }

    public static class PasswordResetTokenBuilder {
        private final PasswordResetToken o = new PasswordResetToken();
        public PasswordResetTokenBuilder id(String v) { o.id = v; return this; }
        public PasswordResetTokenBuilder userId(String v) { o.userId = v; return this; }
        public PasswordResetTokenBuilder codeHash(String v) { o.codeHash = v; return this; }
        public PasswordResetTokenBuilder expiresAt(LocalDateTime v) { o.expiresAt = v; return this; }
        public PasswordResetTokenBuilder attempts(Integer v) { o.attempts = v; return this; }
        public PasswordResetTokenBuilder used(Boolean v) { o.used = v; return this; }
        public PasswordResetTokenBuilder createdAt(LocalDateTime v) { o.createdAt = v; return this; }
        public PasswordResetToken build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
