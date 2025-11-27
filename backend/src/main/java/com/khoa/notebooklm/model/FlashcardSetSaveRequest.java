package com.khoa.notebooklm.model;

import com.khoa.notebooklm.model.Flashcard;

import java.util.List;

public class FlashcardSetSaveRequest {
    private Long userId;
    private String topic;
    private List<Flashcard> flashcards;

    // Constructors
    public FlashcardSetSaveRequest() {
    }

    public FlashcardSetSaveRequest(Long userId, String topic, List<Flashcard> flashcards) {
        this.userId = userId;
        this.topic = topic;
        this.flashcards = flashcards;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getTopic() {
        return topic;
    }

    public List<Flashcard> getFlashcards() {
        return flashcards;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }
}
