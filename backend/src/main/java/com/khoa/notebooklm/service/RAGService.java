package com.khoa.notebooklm.service;

import com.khoa.notebooklm.base_class.Document;
import com.khoa.notebooklm.base_class.PdfDocumentParser;
import com.khoa.notebooklm.base_splitter.MyDocumentSplitter;
import com.khoa.notebooklm.base_splitter.MyDocumentSplitters;
import com.khoa.notebooklm.database.dao.DocumentDAO;
import com.khoa.notebooklm.filter.DocIdFilter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RAGService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatModel;
    private final DocumentDAO documentDAO;

    private final Map<Integer, StudyAssistant> assistantCache = new ConcurrentHashMap<>();

    public RAGService(EmbeddingModel embeddingModel,
                      EmbeddingStore<TextSegment> embeddingStore,
                      ChatLanguageModel chatModel,
                      DocumentDAO documentDAO) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatModel = chatModel;
        this.documentDAO = documentDAO;
    }

    public int ingestDocument(long userId, String fileName, byte[] pdfData) {
        try (InputStream inputStream = new ByteArrayInputStream(pdfData)) {
            System.out.println("--- Starting PDF processing for user: " + userId + " ---");

            // Step 1: Create document record in DB to get an ID
            com.khoa.notebooklm.database.model.Document dbDoc = new com.khoa.notebooklm.database.model.Document(userId, fileName);
            int docId = documentDAO.addDocument(dbDoc);
            System.out.println("📄 Document created in DB with ID: " + docId);

            // Step 2: Parse PDF and add the new DB ID to metadata for all segments
            PdfDocumentParser parser = new PdfDocumentParser();
            Document document = parser.parse(inputStream, fileName);
            document.metadata().put("docId", String.valueOf(docId)); // Use the integer ID from DB

            // Step 3: Split document into segments
            MyDocumentSplitter splitter = MyDocumentSplitters.recursive(500, 0);
            List<TextSegment> segments = splitter.split(document);
            System.out.println("Total segments to process: " + segments.size());

            // Step 4: Create and store embeddings in batches
            int batchSize = 100; // Increased batch size as the DB can handle it
            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<TextSegment> batch = segments.subList(i, end);

                System.out.printf("Embedding and storing batch %d-%d...%n", i, end);
                List<Embedding> embeddings = embeddingModel.embedAll(batch).content();
                embeddingStore.addAll(embeddings, batch);
                System.out.println("✅ Batch " + i + " completed.");
            }

            System.out.println("🎉 Document ingestion complete. DB ID: " + docId);
            return docId;

        } catch (Exception e) {
            // Log the full exception
            throw new RuntimeException("Fatal error during document ingestion: " + e.getMessage(), e);
        }
    }

    public StudyAssistant getOrCreateAssistantForDocument(int docId) {
        return assistantCache.computeIfAbsent(docId, id -> {
            System.out.println("🧠 Creating new Assistant for docId: " + id);
            Filter filter = new DocIdFilter(id);
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
        });
    }
}