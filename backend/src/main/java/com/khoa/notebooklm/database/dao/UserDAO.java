package com.khoa.notebooklm.database.dao;

import com.khoa.notebooklm.database.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class UserDAO {

    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 1. Register new user
    public void registerUser(User user, String encodedPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, password_salt, first_name, last_name, email, date_of_birth) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, encodedPassword);
            stmt.setString(3, ""); // password_salt is not used anymore
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getEmail());
            stmt.setDate(7, Date.valueOf(user.getDateOfBirth()));
            stmt.executeUpdate();
            System.out.println("✅ User registered successfully: " + user.getUsername());
        }
    }

    // 3. Update user info
    public void updateUserInfo(User user) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, date_of_birth = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getDateOfBirth()));
            stmt.setLong(5, user.getId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ User profile updated successfully.");
            } else {
                System.out.println("⚠️ Update failed: User ID not found.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Email already in use by another account.");
        }
    }

    // 4. Update password
    public void updatePassword(long userId, String newEncodedPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newEncodedPassword);
            stmt.setLong(2, userId);

            stmt.executeUpdate();
            System.out.println("🔒 Password changed successfully.");
        }
    }

    // 5. Delete account
    public void deleteUser(long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("🗑️ Account (and all associated data) deleted.");
            }
        }
    }

    public java.util.Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password_hash"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setCreated(rs.getObject("created", LocalDateTime.class));
                user.setLastUpdated(rs.getObject("last_updated", LocalDateTime.class));
                return java.util.Optional.of(user);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return java.util.Optional.empty();
    }
}

