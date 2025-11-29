package knockbooklm.model;

public class Document {
    private int documentId;
    private long userId;
    private String filename;

    public Document() {}

    public Document(long userId, String filename) {
        this.userId = userId;
        this.filename = filename;
    }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
