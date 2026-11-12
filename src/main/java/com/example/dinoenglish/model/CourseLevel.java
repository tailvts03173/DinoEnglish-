package com.example.dinoenglish.model;

public enum CourseLevel {
    BEGINNER_A1("Beginner (A1)", "Tiếng Anh cơ bản, bảng chữ cái, từ vựng thông dụng", "badge-success"),
    ELEMENTARY_A2("Elementary (A2)", "Giao tiếp cơ bản, câu đơn, ngữ pháp nền tảng", "badge-info"),
    INTERMEDIATE_B1("Intermediate (B1)", "Giao tiếp hàng ngày, đọc hiểu và phản xạ", "badge-warning"),
    ADVANCED_B2("Advanced (B2)", "Từ vựng nâng cao, giao tiếp chuyên sâu", "badge-danger");

    private final String title;
    private final String description;
    private final String badgeClass;

    CourseLevel(String title, String description, String badgeClass) {
        this.title = title;
        this.description = description;
        this.badgeClass = badgeClass;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
