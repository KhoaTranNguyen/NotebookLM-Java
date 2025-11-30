package knockbooklm.model.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import knockbooklm.model.Database;
import knockbooklm.model.Flashcard;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlashcardDao {
    private final Gson gson = new Gson();

    public FlashcardDao() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS flashcards (" +
                     "set_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id BIGINT, " +
                     "topic_name VARCHAR(255), " +
                     "flashcards_json TEXT, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        try (Connection c = Database.get().getConnection();
             Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveFlashcardSet(long userId, String topic, List<Flashcard> cards) {
        String sql = "INSERT INTO flashcards (user_id, topic_name, flashcards_json) VALUES (?, ?, ?)";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, topic);
            ps.setString(3, gson.toJson(cards));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FlashcardSetInfo> getFlashcardSetsByUserId(long userId) {
        List<FlashcardSetInfo> sets = new ArrayList<>();
        String sql = "SELECT set_id, topic_name, created_at FROM flashcards WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sets.add(new FlashcardSetInfo(
                        rs.getInt("set_id"),
                        rs.getString("topic_name"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sets;
    }

    public List<Flashcard> getFlashcardsBySetId(int setId) {
        String sql = "SELECT flashcards_json FROM flashcards WHERE set_id = ?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, setId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("flashcards_json");
                    Type listType = new TypeToken<ArrayList<Flashcard>>(){}.getType();
                    return gson.fromJson(json, listType);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>();
    }

    public void deleteFlashcardSet(int setId) {
        String sql = "DELETE FROM flashcards WHERE set_id = ?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, setId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record FlashcardSetInfo(int id, String topic, Timestamp createdAt) {
        @Override
        public String toString() {
            return topic + " (" + createdAt.toLocalDateTime().toLocalDate() + ")";
        }
    }
}
