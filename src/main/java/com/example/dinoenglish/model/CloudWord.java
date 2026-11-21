package com.example.dinoenglish.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cloud_words") // Tên collection trên MongoDB Atlas
public class CloudWord {

    @Id
    private String id;
    private String englishWord;
    private String meaning;

    public CloudWord() {}

    public CloudWord(String englishWord, String meaning) {
        this.englishWord = englishWord;
        this.meaning = meaning;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnglishWord() { return englishWord; }
    public void setEnglishWord(String englishWord) { this.englishWord = englishWord; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
}