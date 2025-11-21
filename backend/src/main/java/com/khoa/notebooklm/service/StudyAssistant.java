package com.khoa.notebooklm.service;

import com.khoa.notebooklm.model.FlashcardResponse;
import com.khoa.notebooklm.model.QuizResponse;
import com.khoa.notebooklm.model.Summary;
import com.khoa.notebooklm.model.PodcastScript;

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


    // --- NHÓM 2: SỬ DỤNG CONTEXT STUFFING (Nhồi toàn bộ nội dung) ---
    // Các hàm này CẦN tham số {{content}} vì ta muốn AI nhìn thấy TOÀN BỘ tài liệu.

    // 4. Tóm tắt thông minh
    @UserMessage("Hãy tóm tắt tài liệu sau thành 3 phần: Tổng quan, Các ý chính, và Hành động đề xuất (nếu có).\n\nNội dung tài liệu:\n{{content}}")
    Summary generateSummary(@V("content") String content);
    
    // 5. Tạo kịch bản Podcast
    @UserMessage("Viết một kịch bản podcast ngắn (khoảng 100 từ) bằng tiếng Việt để tóm tắt nội dung này. Văn phong thân thiện, tự nhiên.\n\nNội dung tài liệu:\n{{content}}")
    PodcastScript generatePodcastScript(@V("content") String content);
}