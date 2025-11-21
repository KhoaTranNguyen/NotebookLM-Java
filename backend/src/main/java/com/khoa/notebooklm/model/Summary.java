package com.khoa.notebooklm.model;

import java.util.List;

/**
 * Định nghĩa cấu trúc cho phần Tóm tắt thông minh.
 * LangChain4j sẽ cố gắng fill data vào đúng các trường này.
 */
public record Summary(
    String overview,        // Tóm tắt tổng quan (đoạn văn ngắn)
    List<String> keyPoints, // Các ý chính (gạch đầu dòng)
    String actionItem       // Hành động đề xuất hoặc bài học rút ra
) {}