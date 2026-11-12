package com.example.dinoenglish.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vocabularies")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 100)
    private String phonetic;

    @Column(length = 50)
    private String partOfSpeech;

    @Column(nullable = false, length = 255)
    private String meaningVi;

    @Column(length = 500)
    private String exampleEn;

    @Column(length = 500)
    private String exampleVi;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String audioUrl;

    public Vocabulary() {
    }

    public Vocabulary(Long id, Lesson lesson, String word, String phonetic, String partOfSpeech,
                      String meaningVi, String exampleEn, String exampleVi, String imageUrl, String audioUrl) {
        this.id = id;
        this.lesson = lesson;
        this.word = word;
        this.phonetic = phonetic;
        this.partOfSpeech = partOfSpeech;
        this.meaningVi = meaningVi;
        this.exampleEn = exampleEn;
        this.exampleVi = exampleVi;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }

    // Builder
    public static VocabularyBuilder builder() {
        return new VocabularyBuilder();
    }

    public static class VocabularyBuilder {
        private Long id;
        private Lesson lesson;
        private String word;
        private String phonetic;
        private String partOfSpeech;
        private String meaningVi;
        private String exampleEn;
        private String exampleVi;
        private String imageUrl;
        private String audioUrl;

        public VocabularyBuilder id(Long id) { this.id = id; return this; }
        public VocabularyBuilder lesson(Lesson lesson) { this.lesson = lesson; return this; }
        public VocabularyBuilder word(String word) { this.word = word; return this; }
        public VocabularyBuilder phonetic(String phonetic) { this.phonetic = phonetic; return this; }
        public VocabularyBuilder partOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; return this; }
        public VocabularyBuilder meaningVi(String meaningVi) { this.meaningVi = meaningVi; return this; }
        public VocabularyBuilder exampleEn(String exampleEn) { this.exampleEn = exampleEn; return this; }
        public VocabularyBuilder exampleVi(String exampleVi) { this.exampleVi = exampleVi; return this; }
        public VocabularyBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public VocabularyBuilder audioUrl(String audioUrl) { this.audioUrl = audioUrl; return this; }

        public Vocabulary build() {
            return new Vocabulary(id, lesson, word, phonetic, partOfSpeech, meaningVi, exampleEn, exampleVi, imageUrl, audioUrl);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

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
