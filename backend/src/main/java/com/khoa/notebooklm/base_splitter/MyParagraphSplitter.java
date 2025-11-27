package com.khoa.notebooklm.base_splitter;

public class MyParagraphSplitter extends MyHierarchicalSplitter {

    public MyParagraphSplitter(int maxChars, int maxOverlap) {
        super(maxChars, maxOverlap, null); // Sub-splitter sẽ được gọi từ default
    }

    public MyParagraphSplitter(int maxChars, int maxOverlap, MyDocumentSplitter subSplitter) {
        super(maxChars, maxOverlap, subSplitter);
    }

    @Override
    protected String[] splitText(String text) {
        // Cắt theo 2 dấu xuống dòng
        return text.split("\\s*\\n\\s*\\n\\s*");
    }

    @Override
    protected String joinDelimiter() {
        return "\n\n";
    }

    @Override
    protected MyDocumentSplitter defaultSubSplitter() {
        // Nếu đoạn to quá -> Gọi thằng Cắt Câu
        return new MySentenceSplitter(maxSegmentSize, maxOverlapSize);
    }
}