package com.example.dinoenglish.model;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(nullable = false, length = 500)
    private String optionA;

    @Column(nullable = false, length = 500)
    private String optionB;

    @Column(nullable = false, length = 500)
    private String optionC;

    @Column(nullable = false, length = 500)
    private String optionD;

    @Column(nullable = false, length = 5)
    private String correctOption; // "A", "B", "C", "D"

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false)
    private Integer orderIndex = 1;

    public QuizQuestion() {
    }

    public QuizQuestion(Long id, Lesson lesson, String question, String optionA, String optionB,
                        String optionC, String optionD, String correctOption, String explanation, Integer orderIndex) {
        this.id = id;
        this.lesson = lesson;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.orderIndex = orderIndex != null ? orderIndex : 1;
    }

    // Builder
    public static QuizQuestionBuilder builder() {
        return new QuizQuestionBuilder();
    }

    public static class QuizQuestionBuilder {
        private Long id;
        private Lesson lesson;
        private String question;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctOption;
        private String explanation;
        private Integer orderIndex = 1;

        public QuizQuestionBuilder id(Long id) { this.id = id; return this; }
        public QuizQuestionBuilder lesson(Lesson lesson) { this.lesson = lesson; return this; }
        public QuizQuestionBuilder question(String question) { this.question = question; return this; }
        public QuizQuestionBuilder optionA(String optionA) { this.optionA = optionA; return this; }
        public QuizQuestionBuilder optionB(String optionB) { this.optionB = optionB; return this; }
        public QuizQuestionBuilder optionC(String optionC) { this.optionC = optionC; return this; }
        public QuizQuestionBuilder optionD(String optionD) { this.optionD = optionD; return this; }
        public QuizQuestionBuilder correctOption(String correctOption) { this.correctOption = correctOption; return this; }
        public QuizQuestionBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public QuizQuestionBuilder orderIndex(Integer orderIndex) { this.orderIndex = orderIndex; return this; }

        public QuizQuestion build() {
            return new QuizQuestion(id, lesson, question, optionA, optionB, optionC, optionD, correctOption, explanation, orderIndex);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
