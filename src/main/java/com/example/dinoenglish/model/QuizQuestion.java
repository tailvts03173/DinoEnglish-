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


/** Embedded trong Lesson.quizQuestions (correctOption: A, B, C, D). */
public class QuizQuestion {

    private String id = UUID.randomUUID().toString();

    private String question;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctOption;

    private String explanation;

    private Integer orderIndex = 1;

    public QuizQuestion() {
    }

    public static QuizQuestionBuilder builder() { return new QuizQuestionBuilder(); }

    public static class QuizQuestionBuilder {
        private final QuizQuestion o = new QuizQuestion();
        public QuizQuestionBuilder id(String v) { o.id = v; return this; }
        public QuizQuestionBuilder question(String v) { o.question = v; return this; }
        public QuizQuestionBuilder optionA(String v) { o.optionA = v; return this; }
        public QuizQuestionBuilder optionB(String v) { o.optionB = v; return this; }
        public QuizQuestionBuilder optionC(String v) { o.optionC = v; return this; }
        public QuizQuestionBuilder optionD(String v) { o.optionD = v; return this; }
        public QuizQuestionBuilder correctOption(String v) { o.correctOption = v; return this; }
        public QuizQuestionBuilder explanation(String v) { o.explanation = v; return this; }
        public QuizQuestionBuilder orderIndex(Integer v) { o.orderIndex = v; return this; }
        public QuizQuestion build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
