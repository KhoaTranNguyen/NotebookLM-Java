package com.khoa.notebooklm.model;

/**
 * Định nghĩa cấu trúc dữ liệu cho kịch bản Podcast.
 */
public record PodcastScript(
    String title,   // Tiêu đề ngắn gọn của đoạn podcast
    String hostName, // Tên nhân vật (ví dụ: "AI Host", "Khoa's Assistant")
    String script   // Nội dung kịch bản chi tiết
) {}