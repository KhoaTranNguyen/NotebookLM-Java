package com.khoa.notebooklm.desktop.model.dao;

import com.khoa.notebooklm.desktop.model.Database;
import com.khoa.notebooklm.desktop.model.DocumentChunk;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChunkDao {

    private final Gson gson = new Gson();

    /**
     * Aggregate text chunks for a document into a single string, limited to maxChars.
     * Assumes table columns: chunk_id, document_id, text_content
     */
    public String aggregateTextByDocument(long documentId, int maxChars) {
        String sql = "SELECT text_content FROM document_chunks WHERE document_id=? ORDER BY chunk_id ASC";
        StringBuilder sb = new StringBuilder();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String t = rs.getString(1);
                    if (t != null && !t.isBlank()) {
                        if (sb.length() + t.length() > maxChars) {
                            int remain = Math.max(0, maxChars - sb.length());
                            sb.append(t, 0, Math.min(remain, t.length()));
                            break;
                        } else {
                            sb.append(t).append('\n');
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public List<Integer> saveChunks(List<DocumentChunk> chunks) throws SQLException {
        String sql = "INSERT INTO document_chunks (document_id, text_content, vector_data) VALUES (?, ?, ?)";
        List<Integer> generatedIds = new ArrayList<>();

        try (Connection conn = Database.get().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (DocumentChunk chunk: chunks) {
                stmt.setInt(1, chunk.getDocumentId());
                stmt.setString(2, chunk.getTextContent());

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
        }
        return generatedIds;
    }

    public List<DocumentChunk> getChunksByDocId(int docId) throws SQLException {
        List<DocumentChunk> chunks = new ArrayList<>();
        String sql = "SELECT chunk_id, text_content, vector_data FROM document_chunks WHERE document_id = ?";

        try (Connection conn = Database.get().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, docId);
            ResultSet rs = stmt.executeQuery();
            Type floatArrayType = new TypeToken<float[]>(){}.getType();

            while (rs.next()) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setChunkId(rs.getInt("chunk_id"));
                chunk.setDocumentId(docId);
                chunk.setTextContent(rs.getString("text_content"));

                String vectorJson = rs.getString("vector_data");
                if (vectorJson != null) {
                    float[] vector = gson.fromJson(vectorJson, floatArrayType);
                    chunk.setVector(vector);
                }

                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
