package com.khoa.notebooklm.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RAGService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatModel;
    
    // Cache RAM cho tốc độ
    private final Map<String, String> documentContentCache = new ConcurrentHashMap<>();
    
    // Cấu hình đường dẫn lưu trữ
    private static final String DATA_DIR = "data/";
    private static final String VECTOR_STORE_PATH = DATA_DIR + "vector-store.json";

    public RAGService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, ChatLanguageModel chatModel) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatModel = chatModel;
        
        // Tạo thư mục data nếu chưa có khi khởi động
        new File(DATA_DIR).mkdirs();
    }

    // --- CƠ CHẾ PERSISTENCE: Lấy nội dung gốc (Ưu tiên RAM -> Ổ cứng) ---
    public String getOriginalContent(String docId) {
        // 1. Tìm trong RAM trước
        if (documentContentCache.containsKey(docId)) {
            return documentContentCache.get(docId);
        }
        
        // 2. Nếu không có, tìm file trên ổ cứng
        try {
            Path filePath = Paths.get(DATA_DIR + docId + ".txt");
            if (Files.exists(filePath)) {
                System.out.println("📂 Đọc nội dung từ ổ cứng cho docId: " + docId);
                String content = Files.readString(filePath);
                // Nạp lại vào RAM dùng cho lần sau
                documentContentCache.put(docId, content);
                return content;
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file content: " + e.getMessage());
        }
        
        return null; // Không tìm thấy
    }

    // --- INGESTION: Xử lý PDF + Lưu trữ bền vững + Safe Mode ---
    public void ingestDocument(String docId, byte[] pdfData) {
        try (InputStream inputStream = new ByteArrayInputStream(pdfData)) {
            System.out.println("--- Bắt đầu xử lý file PDF ---");
            
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);
            document.metadata().put("docId", docId);
            
            // BƯỚC 1: LƯU NỘI DUNG VĂN BẢN (Persistence)
            String content = document.text();
            documentContentCache.put(docId, content); // Lưu RAM
            try {
                Files.writeString(Paths.get(DATA_DIR + docId + ".txt"), content); // Lưu Ổ cứng
                System.out.println("💾 Đã lưu nội dung gốc xuống file: " + DATA_DIR + docId + ".txt");
            } catch (IOException e) {
                System.err.println("⚠️ Không thể lưu file text: " + e.getMessage());
            }

            // BƯỚC 2: CẮT NHỎ TÀI LIỆU
            var splitter = DocumentSplitters.recursive(500, 0);
            List<TextSegment> segments = splitter.split(document);
            System.out.println("Tổng số segments cần xử lý: " + segments.size());

            // BƯỚC 3: TẠO VECTOR (Chiến thuật Ultra Safe để tránh lỗi Quota)
            int batchSize = 3; 
            
            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<TextSegment> batch = segments.subList(i, end);

                boolean success = false;
                int retryCount = 0;

                while (!success && retryCount < 5) {
                    try {
                        System.out.printf("Đang gửi batch %d-%d (Lần thử %d)...%n", i, end, retryCount + 1);
                        
                        List<Embedding> embeddings = embeddingModel.embedAll(batch).content();
                        embeddingStore.addAll(embeddings, batch);
                        
                        success = true;
                        System.out.println("✅ Thành công batch " + i);
                        Thread.sleep(5000); // Nghỉ 5s an toàn

                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("⚠️ Lỗi Quota/Mạng: " + e.getMessage());
                        if (retryCount >= 5) break;
                        
                        long waitTime = 60000; // Nghỉ 60s nếu lỗi
                        System.out.printf("⏳ Đang 'ngủ đông' %d giây...%n", waitTime/1000);
                        try { Thread.sleep(waitTime); } catch (InterruptedException ignored) {}
                    }
                }
            }
            
            // BƯỚC 4: LƯU VECTOR STORE (Persistence)
            if (embeddingStore instanceof InMemoryEmbeddingStore) {
                ((InMemoryEmbeddingStore<TextSegment>) embeddingStore).serializeToFile(Paths.get(VECTOR_STORE_PATH));
                System.out.println("💾 Đã lưu Vector Store xuống file: " + VECTOR_STORE_PATH);
            }

            System.out.println("🎉 HOÀN TẤT INGEST DOCUMENT: " + docId);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi fatal khi đọc PDF: " + e.getMessage(), e);
        }
    }

    // --- RETRIEVAL ---
    public StudyAssistant createAssistantForDocument(String docId) {
        Filter filter = MetadataFilterBuilder.metadataKey("docId").isEqualTo(docId);
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(filter)
                .maxResults(5)
                .minScore(0.7)
                .build();

        return AiServices.builder(StudyAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .contentRetriever(retriever)
                        .build())
                .build();
    }
}