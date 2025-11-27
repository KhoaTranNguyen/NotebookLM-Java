package com.khoa.notebooklm.filter;

import dev.langchain4j.store.embedding.filter.Filter;

public class DocIdFilter implements Filter {
    private final int docId;

    public DocIdFilter(int docId) {
        this.docId = docId;
    }

    public int getDocId() {
        return docId;
    }

    @Override
    public boolean test(Object o) {
        // This filter is only for passing the docId to the custom EmbeddingStore.
        // It is not intended for in-memory filtering.
        return true;
    }
}
