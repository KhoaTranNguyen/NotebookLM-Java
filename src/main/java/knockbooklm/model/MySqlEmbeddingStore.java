package knockbooklm.model;

import knockbooklm.model.dao.ChunkDao;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.Filter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MySqlEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final ChunkDao chunkDao;

    public MySqlEmbeddingStore(ChunkDao chunkDao) {
        this.chunkDao = chunkDao;
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        List<String> ids = addAll(List.of(embedding), List.of(textSegment));
        if (ids == null || ids.isEmpty()) {
            return null;
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
            List<Integer> chunkIds = chunkDao.saveChunks(chunks);
            return chunkIds.stream().map(String::valueOf).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save chunks to database", e);
        }
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        // We require text segments for this store as it's a document store
        throw new UnsupportedOperationException("This store requires TextSegments to be passed with Embeddings");
    }

    @Override
    public void add(String id, Embedding embedding) {
        throw new UnsupportedOperationException("This store requires TextSegments to be passed with Embeddings");
    }

    @Override
    public String add(Embedding embedding) {
        throw new UnsupportedOperationException("This store requires TextSegments to be passed with Embeddings");
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        try {
            Integer docId = parseDocIdFromFilter(request.filter());
            if (docId == null) {
                return new EmbeddingSearchResult<>(new ArrayList<>());
            }

            List<DocumentChunk> chunks = chunkDao.getChunksByDocId(docId);

            List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
            for (DocumentChunk chunk : chunks) {
                if (chunk.getVector() == null) continue;
                
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
            throw new RuntimeException("Failed to search chunks", e);
        }
    }

    private Integer parseDocIdFromFilter(Filter filter) {
        // This is a simplified parser that assumes the filter is a simple equality check on "docId"
        // In a real scenario, you'd need to traverse the Filter object properly.
        // Since LangChain4j Filter structure can be complex, and we know we only filter by docId in this app context:
        if (filter == null) return null;
        // Hacky string parsing because Filter doesn't expose fields easily without casting
        // Or we can assume the caller passes a specific Filter type.
        // Let's try to rely on the fact that we will construct the filter ourselves in RAGController.
        // But wait, EmbeddingSearchRequest comes from the ContentRetriever.
        
        // For now, let's assume we can't easily extract it unless we cast to IsEqualTo
        if (filter instanceof dev.langchain4j.store.embedding.filter.comparison.IsEqualTo) {
             dev.langchain4j.store.embedding.filter.comparison.IsEqualTo f = (dev.langchain4j.store.embedding.filter.comparison.IsEqualTo) filter;
             if ("docId".equals(f.key())) {
                 try {
                     return Integer.parseInt(f.comparisonValue().toString());
                 } catch (NumberFormatException e) {
                     return null;
                 }
             }
        }
        return null;
    }
    
    // Helper for cosine similarity
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
