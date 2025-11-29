package knockbooklm.controller.base_splitter;

public class MySentenceSplitter extends MyHierarchicalSplitter {

    public MySentenceSplitter(int maxChars, int maxOverlap) {
        super(maxChars, maxOverlap, null);
    }

    @Override
    protected String[] splitText(String text) {
        // Regex cắt câu (tìm dấu . ! ? theo sau là khoảng trắng)
        return text.split("(?<=[.!?])\\s+(?=[A-Z])"); 
    }

    @Override
    protected String joinDelimiter() {
        return " ";
    }

    @Override
    protected MyDocumentSplitter defaultSubSplitter() {
        // Nếu câu to quá -> Gọi thằng Cắt Từ
        return new MyWordSplitter(maxSegmentSize, maxOverlapSize);
    }
}
