package com.khoa.notebooklm.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.khoa.notebooklm.database.model.Flashcard;
import com.khoa.notebooklm.model.FlashcardSetInfo;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FlashcardDAO {
    private final Gson gson = new Gson();
    private final DataSource dataSource;

    public FlashcardDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 1. Save Flashcard
    public void saveFlashcardSet(long userId, String topic, List<Flashcard> cards) throws SQLException {
        String sql = "INSERT INTO flashcards (user_id, topic_name, flashcards_json) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, topic);
            stmt.setString(3, gson.toJson(cards));

            stmt.executeUpdate();
        }
    }

    // 2. Get Flashcard
    public List<Flashcard> getFlashcardsBySetId(int setId, long userId) throws SQLException {
        String sql = "SELECT flashcards_json FROM flashcards WHERE set_id = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, setId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String json = rs.getString("flashcards_json");
                Type listType = new TypeToken<ArrayList<Flashcard>>(){}.getType();
                return gson.fromJson(json, listType);
            }
        }
        return new ArrayList<>();
    }

    // 3. Update Flashcard
    public void updateSetProgress(int setId, List<Flashcard> updatedCards, long userId) throws SQLException {
        String sql = "UPDATE flashcards SET flashcards_json = ? WHERE set_id = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String json = gson.toJson(updatedCards);
            stmt.setString(1, json);
            stmt.setInt(2, setId);
            stmt.setLong(3, userId);

            stmt.executeUpdate();
        }
    }

    // 4. Get all Flashcard Sets for a user
    public List<FlashcardSetInfo> getFlashcardSetsByUserId(long userId) throws SQLException {
        List<FlashcardSetInfo> sets = new ArrayList<>();
        String sql = "SELECT set_id, topic_name, created_at FROM flashcards WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sets.add(new FlashcardSetInfo(
                    rs.getInt("set_id"),
                    rs.getString("topic_name"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return sets;
    }

    // 5. Delete Flashcard Set
    public void deleteFlashcardSet(int setId, long userId) throws SQLException {
        String sql = "DELETE FROM flashcards WHERE set_id = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, setId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    // 6. Update Flashcard Set Topic
    public void updateFlashcardSetTopic(int setId, String newTopic, long userId) throws SQLException {
        String sql = "UPDATE flashcards SET topic_name = ? WHERE set_id = ? AND user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTopic);
            stmt.setInt(2, setId);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        }
    }
}
