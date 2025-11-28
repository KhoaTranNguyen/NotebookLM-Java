package com.khoa.notebooklm.desktop.controller.base_splitter;

public class MyWordSplitter extends MyHierarchicalSplitter {

    public MyWordSplitter(int maxChars, int maxOverlap) {
        super(maxChars, maxOverlap, null);
    }

    @Override
    protected String[] splitText(String text) {
        return text.split(" ");
    }

    @Override
    protected String joinDelimiter() {
        return " ";
    }

    @Override
    protected MyDocumentSplitter defaultSubSplitter() {
        return null; // Hết đường rồi, nếu từ vẫn to thì cắt cứng (Hard split)
    }
}
