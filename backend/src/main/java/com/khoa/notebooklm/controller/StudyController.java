package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.model.Flashcard;
import com.khoa.notebooklm.model.FlashcardResponse;
import com.khoa.notebooklm.model.MultipleChoiceQuestion;
import com.khoa.notebooklm.model.QuizResponse;

import com.khoa.notebooklm.service.RAGService;
import com.khoa.notebooklm.service.StudyAssistant;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class StudyController {

    private final RAGService ragService;

    public StudyController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public Map<String, String> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        String docId = UUID.randomUUID().toString();
        ragService.ingestDocument(docId, file.getBytes());
        return Map.of("docId", docId, "message", "Upload thành công!");
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        String query = payload.get("query");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        String answer = assistant.chat(query);
        return Map.of("answer", answer);
    }

    @PostMapping("/flashcards")
    public List<Flashcard> getFlashcards(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        // Gọi qua Wrapper Response rồi lấy list ra để tránh lỗi parse JSON
        return assistant.generateFlashcards(5).flashcards();
    }
    
    @PostMapping("/quiz")
    public List<MultipleChoiceQuestion> getQuiz(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        StudyAssistant assistant = ragService.getOrCreateAssistantForDocument(docId);
        // Tương tự, gọi qua Wrapper Response
        return assistant.generateQuiz(5).questions();
    }
}