package com.khoa.notebooklm.base_class;

import java.util.HashMap;
import java.util.Map;

/* Wrapper class to safely manage document metadata (author, source, etc.), encapsulating a standard Map.
 */
public class Metadata {

    // Storage for key-value pairs; marked:
    // (1) private: to keep it from outside modification
    // (2) "final": to ensure the reference remains immutable.
    private final Map<String, String> entries;

    // Default constructor: Initializes an empty metadata instance, ready for new entries.
    public Metadata() {
        this.entries = new HashMap<>();
    }

    // Overide constructor: Initializes from an existing map using
    // 'Defensive Copy' strategy to protect external data integrity.
    public Metadata(Map<String, String> entries) {
        this.entries = new HashMap<>(entries);
    }

    // Safely adds a key-value pair, automatically ignoring nulls to prevent NullPointerExceptions.
    public void put(String key, String value) {
        if (key != null && value != null) {
            this.entries.put(key, value);
        }
    }

    // Retrieves the value associated with the specified key, or returns null if not found.
    public String get(String key) {
        return this.entries.get(key);
    }

    // Checks if a specific key (e.g., "file_name") exists within the metadata set.
    public boolean containsKey(String key) {
        return this.entries.containsKey(key);
    }

   // Returns a copy of the data as a standard Map ('Defensive Copy'), safe for database persistence or logging.
    public Map<String, String> asMap() {
        return new HashMap<>(this.entries);
    }
    
    // Creates a new independent instance (clone) of this object ('Defensive Copy'), essential for document splitting.
    public Metadata copy() {
        return new Metadata(this.entries);
    }

    // --- UTILS ---
    // Every class in Java are child classes of java.lang.Object parent class.
    // java.lang.Object has 3 default methods, which are toString(), equals() and hashCode()
    @Override
    public String toString() {
        return "Metadata {" + entries + "}";
    }

    @Override
    public boolean equals(Object o) {
        // Compares logical content (the map entries) instead of memory address
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        // HashMap has its own equals()
        return java.util.Objects.equals(entries, metadata.entries);
    }

    @Override
    public int hashCode() {
        // Generates a hash based on content
        return java.util.Objects.hash(entries);
    }
}