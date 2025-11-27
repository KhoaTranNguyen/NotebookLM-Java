package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.database.dao.DocumentDAO;
import com.khoa.notebooklm.database.model.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentDAO documentDAO;

    public DocumentController(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentsForUser() {
        // TODO: Get real userId from Spring Security Context
        long userId = 1L; // Placeholder for the current user

        try {
            List<Document> documents = documentDAO.getDocumentsByUser(userId);
            return ResponseEntity.ok(documents);
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Database error fetching documents."));
        }
    }
}