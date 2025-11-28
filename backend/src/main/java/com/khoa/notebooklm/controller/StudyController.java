package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.model.Flashcard;
import com.khoa.notebooklm.model.MultipleChoiceQuestion;
import com.khoa.notebooklm.model.FlashcardSetSaveRequest;
import com.khoa.notebooklm.model.FlashcardSetInfo;
import com.khoa.notebooklm.database.model.User;

import com.khoa.notebooklm.service.RAGService;
import com.khoa.notebooklm.service.StudyAssistant;
import com.khoa.notebooklm.database.dao.FlashcardDAO;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class StudyController {

    private final RAGService ragService;
    private final FlashcardDAO flashcardDAO;

    public StudyController(RAGService ragService, FlashcardDAO flashcardDAO) {
        this.ragService = ragService;
        this.flashcardDAO = flashcardDAO;
    }

    @GetMapping("/flashcards/sets")
    public ResponseEntity<List<FlashcardSetInfo>> getFlashcardSets(@AuthenticationPrincipal User user) {
        long userId = user.getId();
        try {
            List<FlashcardSetInfo> sets = flashcardDAO.getFlashcardSetsByUserId(userId);
            return ResponseEntity.ok(sets);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/flashcards/set/{setId}")
    public ResponseEntity<List<Flashcard>> getFlashcardSet(@PathVariable int setId, @AuthenticationPrincipal User user) {
        long userId = user.getId();
        try {
            List<com.khoa.notebooklm.database.model.Flashcard> dbCards = flashcardDAO.getFlashcardsBySetId(setId, userId);
            List<Flashcard> apiCards = dbCards.stream()
                .map(dbCard -> new Flashcard(dbCard.getFront(), dbCard.getBack()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(apiCards);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/flashcards/set/{setId}/topic")
    public ResponseEntity<Map<String, String>> updateFlashcardSetTopic(
            @PathVariable int setId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User user) {
        long userId = user.getId();
        String newTopic = payload.get("topic");
        if (newTopic == null || newTopic.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Topic name cannot be empty."));
        }
        
        try {
            flashcardDAO.updateFlashcardSetTopic(setId, newTopic, userId);
            return ResponseEntity.ok(Map.of("message", "Flashcard set topic updated successfully."));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update flashcard set topic."));
        }
    }

    @DeleteMapping("/flashcards/set/{setId}")
    public ResponseEntity<Map<String, String>> deleteFlashcardSet(@PathVariable int setId, @AuthenticationPrincipal User user) {
        long userId = user.getId();
        try {
            flashcardDAO.deleteFlashcardSet(setId, userId);
            return ResponseEntity.ok(Map.of("message", "Flashcard set deleted successfully."));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete flashcard set."));
        }
    }

    @PostMapping("/flashcards/save")
    public Map<String, String> saveFlashcards(@RequestBody FlashcardSetSaveRequest request, @AuthenticationPrincipal User user) {
        long userId = user.getId();
        try {
            List<com.khoa.notebooklm.database.model.Flashcard> dbFlashcards = request.getFlashcards().stream()
                .map(apiCard -> new com.khoa.notebooklm.database.model.Flashcard(apiCard.front(), apiCard.back()))
                .collect(Collectors.toList());

            flashcardDAO.saveFlashcardSet(userId, request.getTopic(), dbFlashcards);
            return Map.of("message", "Flashcard set saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to save flashcard set: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) throws IOException {
        long userId = user.getId();
        String fileName = file.getOriginalFilename();
        int docId = ragService.ingestDocument(userId, fileName, file.getBytes());
        return Map.of("docId", docId, "message", "Upload successful!");
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal User user) {
        int docId = (Integer) payload.get("docId");
        String query = (String) payload.get("query");
        long userId = user.getId();
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId, userId);
        String answer = assistant.chat(query);
        return Map.of("answer", answer);
    }

    @PostMapping("/flashcards")
    public List<Flashcard> getFlashcards(@RequestBody Map<Object, Object> payload, @AuthenticationPrincipal User user) {
        int docId = (Integer) payload.get("docId");
        long userId = user.getId();
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId, userId);
        return assistant.generateFlashcards(5).flashcards();
    }

    @PostMapping("/quiz")
    public List<MultipleChoiceQuestion> getQuiz(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal User user) {
        int docId = (Integer) payload.get("docId");
        long userId = user.getId();
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId, userId);
        return assistant.generateQuiz(5).questions();
    }
}