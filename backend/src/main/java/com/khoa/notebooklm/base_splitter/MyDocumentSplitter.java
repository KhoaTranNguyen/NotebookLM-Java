package com.khoa.notebooklm.base_splitter;

import com.khoa.notebooklm.base_class.Document;
import dev.langchain4j.data.segment.TextSegment;
import java.util.List;
import java.util.stream.Collectors;

public interface MyDocumentSplitter {
    List<TextSegment> split(Document document);

    default List<TextSegment> splitAll(List<Document> documents) {
        return documents.stream()
                .flatMap(document -> this.split(document).stream())
                .collect(Collectors.toList());
    }
}