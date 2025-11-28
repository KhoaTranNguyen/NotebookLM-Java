package com.khoa.notebooklm.desktop.controller;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import com.khoa.notebooklm.desktop.model.dao.ChunkDao;
import com.khoa.notebooklm.desktop.model.MySqlEmbeddingStore;
import com.khoa.notebooklm.desktop.controller.base_class.Document;
import com.khoa.notebooklm.desktop.controller.base_class.PdfDocumentParser;
import com.khoa.notebooklm.desktop.controller.base_splitter.MyDocumentSplitter;
import com.khoa.notebooklm.desktop.controller.base_splitter.MyDocumentSplitters;
import com.khoa.notebooklm.desktop.model.dao.DocumentDao;
import com.khoa.notebooklm.desktop.model.Flashcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RAGController {

    private final VertexAiGeminiChatModel chatModel;
    private final VertexAiEmbeddingModel embeddingModel;
    private final MySqlEmbeddingStore embeddingStore;
    private final DocumentDao documentDao;

    /**
     * Use Google Cloud Project ID and Location.
     * Assumes Application Default Credentials are set up (e.g. gcloud auth application-default login).
     */
    public RAGController() {
        // Hardcoded for now based on backend config, or could be env vars
        this("gemini-langchain4j-codelab", "us-central1");
    }

    public RAGController(String projectId, String location) {
        this.chatModel = VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName("gemini-2.0-flash")
                .maxOutputTokens(1000)
                .build();
        
        this.embeddingModel = VertexAiEmbeddingModel.builder()
                .project(projectId)
                .location(location)
                .endpoint(location + "-aiplatform.googleapis.com:443")
                .publisher("google")
                .modelName("text-embedding-004")
                .build();

        this.embeddingStore = new MySqlEmbeddingStore(new ChunkDao());
        this.documentDao = new DocumentDao();
    }

    public void ingestDocument(long userId, File pdfFile) {
        try (FileInputStream inputStream = new FileInputStream(pdfFile)) {
            System.out.println("--- Starting PDF processing for user: " + userId + " ---");

            // Step 1: Create document record in DB to get an ID
            com.khoa.notebooklm.desktop.model.Document dbDoc = new com.khoa.notebooklm.desktop.model.Document();
            dbDoc.setUserId(userId);
            dbDoc.setFilename(pdfFile.getName());
            
            int docId = documentDao.addDocument(dbDoc);
            System.out.println("📄 Document created in DB with ID: " + docId);

            // Step 2: Parse PDF and add the new DB ID to metadata for all segments
            PdfDocumentParser parser = new PdfDocumentParser();
            Document document = parser.parse(inputStream, pdfFile.getName());
            document.metadata().put("docId", String.valueOf(docId));

            // Step 3: Split document into segments
            MyDocumentSplitter splitter = MyDocumentSplitters.recursive(500, 0);
            List<TextSegment> segments = splitter.split(document);
            System.out.println("Total segments to process: " + segments.size());

            // Step 4: Create and store embeddings in batches
            int batchSize = 100;
            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<TextSegment> batch = segments.subList(i, end);

                System.out.printf("Embedding and storing batch %d-%d...%n", i, end);
                List<Embedding> embeddings = embeddingModel.embedAll(batch).content();
                embeddingStore.addAll(embeddings, batch);
                System.out.println("✅ Batch " + i + " completed.");
            }

            System.out.println("🎉 Document ingestion complete. DB ID: " + docId);

        } catch (Exception e) {
            throw new RuntimeException("Fatal error during document ingestion: " + e.getMessage(), e);
        }
    }

    public void ingestDocument(File file) {
        // ...existing code...
        // (Assuming ingestDocument is here, I'll add deleteDocument after it or at the end)
    }

    public void deleteDocument(long docId) {
        try {
            documentDao.deleteChunksByDocId(docId);
            documentDao.deleteDocument(docId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete document: " + e.getMessage());
        }
    }

    public String chat(String prompt) {
        return chatModel.generate(prompt);
    }

    public String summarize(String text) {
        String prompt = "Summarize the following document in Vietnamese with key points and a short abstract.\n\n" + text;
        return chatModel.generate(prompt);
    }

    public String[] generateFlashcards(String text, int count) {
        String prompt = "Create " + count + " concise Q&A flashcards from the following content. Return as lines: question :: answer.\n\n" + text;
        String out = chatModel.generate(prompt);
        return out.split("\n");
    }

    public GeneratedFlashcardSet generateFlashcardSet(long documentId, int count) {
        ChunkDao dao = new ChunkDao();
        String context = dao.aggregateTextByDocument(documentId, 12000);
        
        // 1. Generate Flashcards
        String[] lines = generateFlashcards(context, count);
        List<Flashcard> cards = new ArrayList<>();
        for (String line : lines) {
            String[] qa = line.split("::");
            if (qa.length >= 2) {
                cards.add(new Flashcard(qa[0].trim(), qa[1].trim()));
            }
        }

        // 2. Generate Title
        String titlePrompt = "Generate a short, concise topic title (max 5 words) for a flashcard set based on the following content. Return ONLY the title.\n\n" + context;
        String title = chatModel.generate(titlePrompt).trim().replace("\"", "");

        return new GeneratedFlashcardSet(title, cards);
    }

    public String[] generateFlashcardsFromDocument(long documentId, int count) {
        ChunkDao dao = new ChunkDao();
        String context = dao.aggregateTextByDocument(documentId, 12000);
        return generateFlashcards(context, count);
    }

    public record GeneratedFlashcardSet(String title, List<Flashcard> cards) {}

    /**
     * Chat with context assembled from DB document chunks for the given document.
     */
    public String chatWithDocument(long documentId, String question) {
        // 1. Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // 2. Search for relevant chunks
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .filter(new IsEqualTo("docId", (int)documentId)) // Cast to int as DB uses int for docId usually
                .maxResults(5)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        
        // 3. Construct context
        String context;
        if (searchResult.matches().isEmpty()) {
            // Fallback to simple aggregation if no vectors found or no match
            ChunkDao dao = new ChunkDao();
            context = dao.aggregateTextByDocument(documentId, 12000);
        } else {
            context = searchResult.matches().stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.joining("\n\n"));
        }

        String prompt = "Use the provided document context to answer the question.\n\n" +
                "# Context\n" + context + "\n\n# Question\n" + question + "\n\n# Answer:";
        return chatModel.generate(prompt);
    }
}
