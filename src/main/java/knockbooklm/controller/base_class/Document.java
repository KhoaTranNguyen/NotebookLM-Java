package knockbooklm.controller.base_class;

import java.util.Objects;

public class Document {

    // Format Key (Magic String)
    public static final String FILE_NAME = "file_name"; 

    private final String text;
    private final Metadata metadata;

    private Document(String text, Metadata metadata) {
        if (text == null) {
            throw new IllegalArgumentException("Document text cannot be null");
        }
        this.text = text;
        this.metadata = metadata != null ? metadata : new Metadata();
    }

    // --- STATIC FACTORY METHODS ---

    public static Document from(String text) {
        return new Document(text, new Metadata());
    }

    public static Document from(String text, Metadata metadata) {
        return new Document(text, metadata);
    }

    // --- GETTERS ---

    public String text() {
        return text;
    }

    public Metadata metadata() {
        return metadata;
    }

    // quickly get file name
    public String fileName() {
         // if fileName = null -> FILE_NAME entry will not exist in metadata
        return metadata.get(FILE_NAME);
    }

    // --- UTILS ---
    // Every class in Java are child classes of java.lang.Object parent class.
    // java.lang.Object has 3 default methods, which are toString(), equals() and hashCode()
    @Override
    public String toString() {
        // Update toString for showing filename + 50 characters of content
        String name = fileName() != null ? fileName() : "unknown";
        String preview = text.length() > 50 ? text.substring(0, 50) + "..." : text;
        return "Document { source=\"" + name + "\", text=\"" + preview + "\", metadata=" + metadata + " }";
    }

    @Override
    public boolean equals(Object o) {
      // Defines logical equality based on content (text & metadata) rather than object identity (memory reference),
      // allowing distinct instances with the same data to be treated as equal.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(text, document.text) &&
               Objects.equals(metadata, document.metadata);
    }

    @Override
    public int hashCode() {
      // Generates a hash based on content (text/metadata) instead of memory address
      // to ensure correct behavior in HashMaps and HashSets.
        return Objects.hash(text, metadata);
    }
}
