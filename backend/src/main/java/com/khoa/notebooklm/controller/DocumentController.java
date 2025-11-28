package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.database.dao.DocumentDAO;
import com.khoa.notebooklm.database.model.Document;
import com.khoa.notebooklm.database.model.User;
import com.khoa.notebooklm.service.RAGService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentDAO documentDAO;
    private final RAGService ragService;

    public DocumentController(DocumentDAO documentDAO, RAGService ragService) {
        this.documentDAO = documentDAO;
        this.ragService = ragService;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentsForUser(@AuthenticationPrincipal User user) {
        long userId = user.getId();

        try {
            List<Document> documents = documentDAO.getDocumentsByUser(userId);
            return ResponseEntity.ok(documents);
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Database error fetching documents."));
        }
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteDocument(@PathVariable int docId, @AuthenticationPrincipal User user) {
        long userId = user.getId();
        try {
            // TODO: Add security check to ensure user owns this document
            ragService.deleteDocumentAndChunks(docId, userId);
            return ResponseEntity.ok(Map.of("message", "Document and associated chunks deleted successfully."));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete document."));
        }
    }
}