package com.khoa.notebooklm.database;

import com.khoa.notebooklm.database.dao.DocumentDAO;
import com.khoa.notebooklm.database.model.DocumentChunk;
import com.khoa.notebooklm.filter.DocIdFilter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.Filter;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MySqlEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final DocumentDAO documentDAO;

    public MySqlEmbeddingStore(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    // Main methods to be implemented

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        List<String> ids = addAll(List.of(embedding), List.of(textSegment));
        if (ids == null || ids.isEmpty()) {
            return null; // Or throw an exception
        }
        return ids.get(0);
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        if (embeddings == null || textSegments == null || embeddings.size() != textSegments.size()) {
            throw new IllegalArgumentException("Embeddings and text segments must not be null and must have the same size");
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            TextSegment segment = textSegments.get(i);
            String docIdStr = segment.metadata().getString("docId");
            if (docIdStr == null) {
                continue;
            }
            int docId;
            try {
                docId = Integer.parseInt(docIdStr);
            } catch (NumberFormatException e) {
                continue;
            }

            chunks.add(new DocumentChunk(docId, segment.text(), embeddings.get(i).vector()));
        }

        if (chunks.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<Integer> chunkIds = documentDAO.saveChunks(chunks);
            return chunkIds.stream().map(String::valueOf).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save chunks to database", e);
        }
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        try {
            Integer docId = parseDocIdFromFilter(request.filter());
            if (docId == null) {
                return new EmbeddingSearchResult<>(new ArrayList<>());
            }

            List<DocumentChunk> chunks = documentDAO.getChunksByDocId(docId);

            List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
            for (DocumentChunk chunk : chunks) {
                Embedding embedding = Embedding.from(chunk.getVector());
                double score = RelevanceScore.fromCosineSimilarity(cosineSimilarity(request.queryEmbedding().vector(), embedding.vector()));
                if (score >= request.minScore()) {
                    TextSegment segment = TextSegment.from(chunk.getTextContent());
                    matches.add(new EmbeddingMatch<>(score, String.valueOf(chunk.getChunkId()), embedding, segment));
                }
            }

            matches.sort((m1, m2) -> m2.score().compareTo(m1.score()));

            List<EmbeddingMatch<TextSegment>> limitedMatches = matches.stream()
                    .limit(request.maxResults())
                    .collect(Collectors.toList());

            return new EmbeddingSearchResult<>(limitedMatches);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve chunks from database", e);
        }
    }

    // Helper methods

    private Integer parseDocIdFromFilter(Filter filter) {
        if (filter instanceof DocIdFilter) {
            return ((DocIdFilter) filter).getDocId();
        }
        return null;
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Unimplemented methods from EmbeddingStore interface

    @Override
    public String add(Embedding embedding) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(String id, Embedding embedding) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        throw new UnsupportedOperationException("Not supported.");
    }
}