package com.khoa.notebooklm.database.model;

public class Flashcard {
    private String front;
    private String back;

    public Flashcard() {}

    public Flashcard(String front, String back) {
        this.front = front;
        this.back = back;
    }

    // Getters and Setters
    public String getFront() { return front; }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() { return back; }

    public void setBack(String back) {
        this.back = back;
    }
}
