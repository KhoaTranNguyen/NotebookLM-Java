package com.khoa.notebooklm.database.model;

public class DocumentChunk {
    private int chunkId;
    private int documentId;
    private String textContent;
    private float[] vector;

    public DocumentChunk() {}

    public DocumentChunk(int documentId, String textContent, float[] vector) {
        this.documentId = documentId;
        this.textContent = textContent;
        this.vector = vector;
    }

    // Getters and Setters
    public int getChunkId() { return chunkId; }
    public void setChunkId(int chunkId) { this.chunkId = chunkId; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public float[] getVector() { return vector; }
    public void setVector(float[] vector) { this.vector = vector; }
}
