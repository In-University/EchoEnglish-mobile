package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import java.io.Serializable;

// Removed Lombok annotations
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

public class VocabularyResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String word;
    private String definition;
    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
    // <-- Added flashcardId field -->
    private Long flashcardId;
    // --> End added flashcardId field -->


    // No-argument constructor (required for serialization and some libraries)
    public VocabularyResponse() {
    }

    // Constructor with all fields (optional, but useful)
    public VocabularyResponse(Long id, String word, String definition, String phonetic, String example, String type, String imageUrl, Long flashcardId) {
        this.id = id;
        this.word = word;
        this.definition = definition;
        this.phonetic = phonetic;
        this.example = example;
        this.type = type;
        this.imageUrl = imageUrl;
        this.flashcardId = flashcardId;
    }


    // Getters
    public Long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public String getExample() {
        return example;
    }

    public String getType() {
        return type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // <-- Added getFlashcardId getter -->
    public Long getFlashcardId() {
        return flashcardId;
    }
    // --> End added getFlashcardId getter -->


    // Setters (optional, but often needed)
    public void setId(Long id) {
        this.id = id;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // <-- Added setFlashcardId setter -->
    public void setFlashcardId(Long flashcardId) {
        this.flashcardId = flashcardId;
    }
    // --> End added setFlashcardId setter -->

    // Optional: Override toString() for logging/debugging
    @Override
    public String toString() {
        return "VocabularyResponse{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", phonetic='" + phonetic + '\'' +
                ", example='" + example + '\'' +
                ", type='" + type + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", flashcardId=" + flashcardId +
                '}';
    }
}