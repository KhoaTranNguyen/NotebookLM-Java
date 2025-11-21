package com.khoa.notebooklm.model;

/**
 * Định nghĩa cấu trúc dữ liệu cho một thẻ ghi nhớ (Flashcard).
 * Dùng Java Record để code ngắn gọn (tự động có getter, constructor, toString).
 */
public record Flashcard(
    String front,  // Mặt trước: Câu hỏi hoặc thuật ngữ
    String back    // Mặt sau: Câu trả lời hoặc định nghĩa
) {}