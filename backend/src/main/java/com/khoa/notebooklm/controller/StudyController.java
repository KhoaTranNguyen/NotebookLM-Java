package com.khoa.notebooklm.controller;

import com.khoa.notebooklm.model.Flashcard;
import com.khoa.notebooklm.model.FlashcardResponse;
import com.khoa.notebooklm.model.MultipleChoiceQuestion;
import com.khoa.notebooklm.model.QuizResponse;
import com.khoa.notebooklm.model.Summary;
import com.khoa.notebooklm.model.PodcastScript;
import com.khoa.notebooklm.service.RAGService;
import com.khoa.notebooklm.service.StudyAssistant;
import com.khoa.notebooklm.service.TtsService;
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
    private final TtsService ttsService;

    public StudyController(RAGService ragService, TtsService ttsService) {
        this.ragService = ragService;
        this.ttsService = ttsService;
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
        StudyAssistant assistant = ragService.createAssistantForDocument(docId);
        String answer = assistant.chat(query);
        return Map.of("answer", answer);
    }

    @PostMapping("/flashcards")
    public List<Flashcard> getFlashcards(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        StudyAssistant assistant = ragService.createAssistantForDocument(docId);
        // Gọi qua Wrapper Response rồi lấy list ra để tránh lỗi parse JSON
        return assistant.generateFlashcards(5).flashcards();
    }
    
    @PostMapping("/quiz")
    public List<MultipleChoiceQuestion> getQuiz(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        StudyAssistant assistant = ragService.createAssistantForDocument(docId);
        // Tương tự, gọi qua Wrapper Response
        return assistant.generateQuiz(5).questions();
    }

    @PostMapping("/summary")
    public Summary getSummary(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        String content = payload.get("content");
        
        // Fallback: Nếu content null, lấy từ bộ nhớ Cache (RAM/Disk) của Server
        if (content == null || content.trim().isEmpty()) {
            content = ragService.getOriginalContent(docId);
        }
        
        if (content == null) {
            throw new IllegalArgumentException("Không tìm thấy nội dung. Hãy upload lại file.");
        }

        StudyAssistant assistant = ragService.createAssistantForDocument(docId);
        return assistant.generateSummary(content);
    }

    @PostMapping("/podcast")
    public PodcastScript getPodcast(@RequestBody Map<String, String> payload) {
        String docId = payload.get("docId");
        String content = payload.get("content");
        
        // Fallback: Nếu content null, lấy từ bộ nhớ Cache
        if (content == null || content.trim().isEmpty()) {
            content = ragService.getOriginalContent(docId);
        }

        if (content == null) {
            throw new IllegalArgumentException("Không tìm thấy nội dung. Hãy upload lại file.");
        }

        StudyAssistant assistant = ragService.createAssistantForDocument(docId);
        return assistant.generatePodcastScript(content);
    }

    // API tạo âm thanh từ văn bản (TTS)
    @PostMapping("/tts")
    public ResponseEntity<byte[]> generateAudio(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        byte[] audioBytes = ttsService.synthesizeText(text);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audioBytes);
    }
}