package com.example.dinoenglish.model;

public enum LessonType {
    VOCABULARY("Từ vựng", "fa-book-open", "text-success"),
    GRAMMAR("Ngữ pháp", "fa-spell-check", "text-primary"),
    LISTENING("Luyện nghe", "fa-headphones", "text-info"),
    READING("Luyện đọc", "fa-glasses", "text-warning"),
    QUIZ("Kiểm tra trắc nghiệm", "fa-question-circle", "text-danger"),
    COMPREHENSIVE("Tổng hợp", "fa-star", "text-purple");

    private final String title;
    private final String icon;
    private final String colorClass;

    LessonType(String title, String icon, String colorClass) {
        this.title = title;
        this.icon = icon;
        this.colorClass = colorClass;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorClass() {
        return colorClass;
    }
}
