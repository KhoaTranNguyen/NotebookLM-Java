package com.khoa.notebooklm.model;

public class FlashcardSetInfo {
    private int setId;
    private String topicName;
    private java.sql.Timestamp createdAt;

    public FlashcardSetInfo(int setId, String topicName, java.sql.Timestamp createdAt) {
        this.setId = setId;
        this.topicName = topicName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getSetId() {
        return setId;
    }

    public void setSetId(int setId) {
        this.setId = setId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
