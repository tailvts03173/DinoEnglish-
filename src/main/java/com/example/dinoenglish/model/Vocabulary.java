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


/** Embedded trong Lesson.vocabularies (không có collection riêng). */
public class Vocabulary {

    private String id = UUID.randomUUID().toString();

    private String word;

    private String phonetic;

    private String partOfSpeech;

    private String meaningVi;

    private String exampleEn;

    private String exampleVi;

    private String imageUrl;

    private String audioUrl;

    public Vocabulary() {
    }

    public static VocabularyBuilder builder() { return new VocabularyBuilder(); }

    public static class VocabularyBuilder {
        private final Vocabulary o = new Vocabulary();
        public VocabularyBuilder id(String v) { o.id = v; return this; }
        public VocabularyBuilder word(String v) { o.word = v; return this; }
        public VocabularyBuilder phonetic(String v) { o.phonetic = v; return this; }
        public VocabularyBuilder partOfSpeech(String v) { o.partOfSpeech = v; return this; }
        public VocabularyBuilder meaningVi(String v) { o.meaningVi = v; return this; }
        public VocabularyBuilder exampleEn(String v) { o.exampleEn = v; return this; }
        public VocabularyBuilder exampleVi(String v) { o.exampleVi = v; return this; }
        public VocabularyBuilder imageUrl(String v) { o.imageUrl = v; return this; }
        public VocabularyBuilder audioUrl(String v) { o.audioUrl = v; return this; }
        public Vocabulary build() { return o; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getMeaningVi() { return meaningVi; }
    public void setMeaningVi(String meaningVi) { this.meaningVi = meaningVi; }

    public String getExampleEn() { return exampleEn; }
    public void setExampleEn(String exampleEn) { this.exampleEn = exampleEn; }

    public String getExampleVi() { return exampleVi; }
    public void setExampleVi(String exampleVi) { this.exampleVi = exampleVi; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

}
