package com.example.dinoenglish.model;

public enum Role {
    ADMIN("Quản trị viên"),
    MANAGER("Quản lí nội dung"),
    USER("Học viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
