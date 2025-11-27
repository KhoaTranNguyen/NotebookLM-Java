package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.model.Flashcard;
import com.khoa.notebooklm.model.MultipleChoiceQuestion;

import com.khoa.notebooklm.service.RAGService;
import com.khoa.notebooklm.service.StudyAssistant;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class StudyController {

    private final RAGService ragService;

    public StudyController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        // TODO: Get userId from Spring Security context
        long userId = 1L; // Placeholder for the logged-in user
        String fileName = file.getOriginalFilename();
        int docId = ragService.ingestDocument(userId, fileName, file.getBytes());
        return Map.of("docId", docId, "message", "Upload successful!");
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, Object> payload) {
        int docId = (Integer) payload.get("docId");
        String query = (String) payload.get("query");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        String answer = assistant.chat(query);
        return Map.of("answer", answer);
    }

    @PostMapping("/flashcards")
    public List<Flashcard> getFlashcards(@RequestBody Map<String, Object> payload) {
        int docId = (Integer) payload.get("docId");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        return assistant.generateFlashcards(5).flashcards();
    }

    @PostMapping("/quiz")
    public List<MultipleChoiceQuestion> getQuiz(@RequestBody Map<String, Object> payload) {
        int docId = (Integer) payload.get("docId");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        return assistant.generateQuiz(5).questions();
    }
}