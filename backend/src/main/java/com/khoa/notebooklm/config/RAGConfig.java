package com.khoa.notebooklm.config;

import com.khoa.notebooklm.database.MySqlEmbeddingStore;
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

}