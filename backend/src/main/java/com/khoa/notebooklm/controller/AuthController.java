package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.database.dao.UserDAO;
import com.khoa.notebooklm.database.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserDAO userDAO;

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required."));
        }

        try {
            User user = userDAO.loginUser(username, password);
            if (user != null) {
                // In a real app, you'd generate a JWT or use a session
                // For now, returning the user object is enough for the frontend
                user.setPasswordHash(null); // Don't send sensitive info
                user.setPasswordSalt(null);
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Database error during login."));
        }
    }

    // A simplified registration for the demo
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, @RequestParam String password) {
         try {
            userDAO.registerUser(user, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Database error during registration: " + e.getMessage()));
        }
    }
}