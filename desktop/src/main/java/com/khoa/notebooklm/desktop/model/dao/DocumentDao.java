package com.khoa.notebooklm.desktop.model.dao;

import com.khoa.notebooklm.desktop.model.Database;
import com.khoa.notebooklm.desktop.model.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocumentDao {

    public int addDocument(Document doc) throws SQLException {
        String sql = "INSERT INTO documents (user_id, filename) VALUES (?, ?)";

        try (Connection conn = Database.get().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, doc.getUserId());
            stmt.setString(2, doc.getFilename());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating document failed, no ID obtained.");
    }

    public List<DocumentRow> listDocuments() {
        String sql = "SELECT document_id, filename FROM documents ORDER BY document_id DESC"; // keep schema as-is
        List<DocumentRow> out = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new DocumentRow(rs.getLong(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    /**
     * Returns textual content used for RAG. If your schema stores chunks, this can aggregate them.
     */
    public String getDocumentText(long documentId) {
        // Option A: read from a table that stores full text
        String sql = "SELECT content FROM documents WHERE document_id=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteDocument(long docId) throws SQLException {
        String sql = "DELETE FROM documents WHERE document_id = ?";
        try (Connection conn = Database.get().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, docId);
            stmt.executeUpdate();
        }
    }

    public void deleteChunksByDocId(long docId) throws SQLException {
        String sql = "DELETE FROM document_chunks WHERE document_id = ?";
        try (Connection conn = Database.get().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, docId);
            stmt.executeUpdate();
        }
    }

    public record DocumentRow(long id, String filename) {}
}
