package com.khoa.notebooklm.desktop.model.dao;

import com.khoa.notebooklm.desktop.model.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.khoa.notebooklm.desktop.controller.PasswordUtil;

public class UserDao {

    public boolean checkCredentials(String username, String password) {
        String sql = "SELECT password_hash, password_salt FROM users WHERE username=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String storedHash = rs.getString(1);
                String storedSalt = rs.getString(2);
                return PasswordUtil.verify(password.toCharArray(), storedSalt, storedHash);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createUser(String username, String rawPassword, String firstName, String lastName, String email, LocalDate dob) {
        if (userExists(username)) throw new IllegalArgumentException("Username already exists");
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(rawPassword.toCharArray(), salt);
        String sql = "INSERT INTO users(username, password_hash, password_salt, first_name, last_name, email, date_of_birth, created, last_updated) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, firstName);
            ps.setString(5, lastName);
            ps.setString(6, email);
            ps.setObject(7, dob); // assuming DATE
            LocalDateTime now = LocalDateTime.now();
            ps.setObject(8, now);
            ps.setObject(9, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
