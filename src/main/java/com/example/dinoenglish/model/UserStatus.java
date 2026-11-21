package com.example.dinoenglish.model;

public enum UserStatus {
    ACTIVE("Hoạt động"),
    BLOCKED("Bị khóa");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
