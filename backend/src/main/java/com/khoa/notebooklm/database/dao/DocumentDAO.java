package com.khoa.notebooklm.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.khoa.notebooklm.database.model.Document;
import com.khoa.notebooklm.database.model.DocumentChunk;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DocumentDAO {
    private final Gson gson = new Gson();
    private final DataSource dataSource;

    public DocumentDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 1. Save document (Vector -> JSON)
    public int addDocument(Document doc) throws SQLException {
        String sql = "INSERT INTO documents (user_id, filename) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, doc.getUserId());
            stmt.setString(2, doc.getFilename());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    System.out.println("✅ Document saved: " + doc.getFilename());
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating document failed, no ID obtained.");
    }

    // 2. Save chunks
    public List<Integer> saveChunks(List<DocumentChunk> chunks) throws SQLException {
        String sql = "INSERT INTO document_chunks (document_id, text_content, vector_data) VALUES (?, ?, ?)";
        List<Integer> generatedIds = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Use Batch for speed (Saving 1000 chunks one by one is slow)
            for (DocumentChunk chunk: chunks) {
                stmt.setInt(1, chunk.getDocumentId());
                stmt.setString(2, chunk.getTextContent());

                // Convert Vector (float[]) to JSON String
                String vectorJson = gson.toJson(chunk.getVector());
                stmt.setString(3, vectorJson);

                stmt.addBatch();
            }
            stmt.executeBatch();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                while (generatedKeys.next()) {
                    generatedIds.add(generatedKeys.getInt(1));
                }
            }

            System.out.println("✅ Saved " + chunks.size() + " chunks to DB.");
            if (generatedIds.size() != chunks.size()) {
                 System.err.println("⚠️ Warning: Number of generated IDs does not match number of chunks.");
            }
        }
        return generatedIds;
    }

    // 3. Get chunks
    public List<DocumentChunk> getChunksByDocId(int docId) throws SQLException {
        List<DocumentChunk> chunks = new ArrayList<>();
        String sql = "SELECT chunk_id, text_content, vector_data FROM document_chunks WHERE document_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, docId);
            ResultSet rs = stmt.executeQuery();
            Type floatArrayType = new TypeToken<float[]>(){}.getType();

            while (rs.next()) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setChunkId(rs.getInt("chunk_id"));
                chunk.setDocumentId(docId);
                chunk.setTextContent(rs.getString("text_content"));

                // Convert JSON -> float[]
                String vectorJson = rs.getString("vector_data");
                float[] vector = gson.fromJson(vectorJson, floatArrayType);
                chunk.setVector(vector);

                chunks.add(chunk);
            }
        }
        return chunks;
    }

    // 4. Get documents by user (JSON -> Vector)
    public List<Document> getDocumentsByUser(long userId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Document doc = new Document();
                doc.setDocumentId(rs.getInt("document_id"));
                doc.setUserId(rs.getLong("user_id"));
                doc.setFilename(rs.getString("filename"));
                docs.add(doc);
            }
        }
        return docs;
    }

    // 5. Rename a file
    public void updateDocumentName(int docId, String newFilename) throws SQLException {
        String sql = "UPDATE documents SET filename = ? WHERE document_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newFilename);
            stmt.setInt(2, docId);

            stmt.executeUpdate();
            System.out.println("✏️ Document renamed.");
        }
    }

    // 6. Delete a single file
    public void deleteDocument(int docId) throws SQLException {
        String sql = "DELETE FROM documents WHERE document_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, docId);

            stmt.executeUpdate();
            System.out.println("🗑️ Document deleted.");
        }
    }

    // 7. Delete all chunks for a document
    public void deleteChunksByDocId(int docId) throws SQLException {
        String sql = "DELETE FROM document_chunks WHERE document_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, docId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("🗑️ Deleted " + rowsAffected + " chunks for document ID: " + docId);
        }
    }
}
