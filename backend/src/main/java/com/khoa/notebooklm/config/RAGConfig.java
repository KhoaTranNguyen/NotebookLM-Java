package com.khoa.notebooklm.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class RAGConfig {

    private final String projectId = "geminijava-478112"; 
    private final String location = "us-central1";

    // Đường dẫn file lưu vector trên ổ cứng
    private static final String VECTOR_STORE_PATH = "data/vector-store.json";

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName("gemini-2.0-flash")
                .maxOutputTokens(1000)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return VertexAiEmbeddingModel.builder()
                .project(projectId)
                .location(location)
                .endpoint(location + "-aiplatform.googleapis.com:443")
                .publisher("google")
                .modelName("text-embedding-004")
                .build();
    }

    // Lưu trữ Vector trong folder (giống file RAG.java của bạn)
    // Trong thực tế production, chỗ này sẽ là Pinecone, Milvus hoặc PostgreSQL pgvector
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // Tạo thư mục data nếu chưa có
        new File("data").mkdirs();

        File storeFile = new File(VECTOR_STORE_PATH);
        if (storeFile.exists()) {
            System.out.println("📂 Đang tải Vector Store từ file: " + VECTOR_STORE_PATH);
            return InMemoryEmbeddingStore.fromFile(Paths.get(VECTOR_STORE_PATH));
        } else {
            System.out.println("🆕 Tạo mới Vector Store (Chưa có dữ liệu cũ)");
            return new InMemoryEmbeddingStore<>();
        }
    }
}