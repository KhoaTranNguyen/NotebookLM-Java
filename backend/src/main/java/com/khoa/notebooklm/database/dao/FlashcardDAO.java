package com.khoa.notebooklm.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.khoa.notebooklm.database.model.Flashcard;
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
    public List<Flashcard> getFlashcardsBySetId(int setId) throws SQLException {
        String sql = "SELECT flashcards_json FROM flashcards WHERE set_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, setId);
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
    public void updateSetProgress(int setId, List<Flashcard> updatedCards) throws SQLException {
        String sql = "UPDATE flashcards SET flashcards_json = ? WHERE set_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String json = gson.toJson(updatedCards);
            stmt.setString(1, json);
            stmt.setInt(2, setId);

            stmt.executeUpdate();
        }
    }
}
