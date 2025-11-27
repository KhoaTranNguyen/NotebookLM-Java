package com.khoa.notebooklm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;

@SpringBootApplication
public class NotebookLmApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotebookLmApplication.class, args);
        System.out.println("🚀 NotebookLM Java Backend is running at http://localhost:8080");
    }

    // --- CẤU HÌNH CỨNG GIỚI HẠN UPLOAD ---
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Cho phép file lẻ lên tới 100MB
        factory.setMaxFileSize(DataSize.ofMegabytes(100));
        
        // Cho phép tổng dung lượng request lên tới 100MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        
        return factory.createMultipartConfig();
    }
}