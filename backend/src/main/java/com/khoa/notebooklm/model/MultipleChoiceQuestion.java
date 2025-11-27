package com.khoa.notebooklm.model;

import java.util.List;

/**
 * Định nghĩa cấu trúc cho một câu hỏi trắc nghiệm.
 */
public record MultipleChoiceQuestion(
    String question,        // Nội dung câu hỏi
    List<String> options,   // Danh sách 4 lựa chọn (A, B, C, D)
    int correctIndex        // Chỉ số của đáp án đúng (0, 1, 2, hoặc 3)
) {}