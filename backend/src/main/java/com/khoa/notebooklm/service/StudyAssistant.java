package com.khoa.notebooklm.service;

import com.khoa.notebooklm.model.FlashcardResponse;
import com.khoa.notebooklm.model.QuizResponse;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface StudyAssistant {

    // --- NHÓM 1: SỬ DỤNG RAG (Retrieval Augmented Generation) ---
    // Các hàm này KHÔNG cần tham số {{content}} vì RAG sẽ tự động tìm 
    // các đoạn (chunks) liên quan và "nhồi" vào ngữ cảnh ngầm bên dưới.

    // 1. Chat với tài liệu
    @SystemMessage("Bạn là trợ lý học tập thông minh. Hãy trả lời câu hỏi dựa trên thông tin được cung cấp. Nếu không tìm thấy thông tin để trả lời, hãy nói thành thật là bạn không biết.")
    @UserMessage("{{query}}")
    String chat(@V("query") String query);

    // 2. Tạo Flashcards
    // Quan trọng: Trả về FlashcardResponse (Wrapper) thay vì List để tránh lỗi JSON
    @UserMessage("Tạo {{count}} flashcards quan trọng nhất dựa trên thông tin bạn tìm thấy được. Trả về định dạng JSON.")
    FlashcardResponse generateFlashcards(@V("count") int count);

    // 3. Tạo Quiz
    // Quan trọng: Trả về QuizResponse (Wrapper)
    @UserMessage("Tạo {{count}} câu hỏi trắc nghiệm (4 lựa chọn) dựa trên thông tin ngữ cảnh này. Trả về định dạng JSON.")
    QuizResponse generateQuiz(@V("count") int count);
}