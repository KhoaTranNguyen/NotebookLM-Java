package com.khoa.notebooklm.base_class;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata; // Import Metadata of langchain4k for ouput

import java.util.ArrayList;
import java.util.List;

public class CustomRecursiveSplitter {

    private final int maxSegmentSize;
    private final int maxOverlapSize;
    
    // Danh sách các ký tự phân cách theo thứ tự ưu tiên
    // 1. Đoạn văn (\n\n) -> 2. Dòng (\n) -> 3. Câu (.) -> 4. Từ (dấu cách)
    private final String[] separators = {"\n\n", "\n", "\\. ", " "};

    public CustomRecursiveSplitter(int maxSegmentSize, int maxOverlapSize) {
        this.maxSegmentSize = maxSegmentSize;
        this.maxOverlapSize = maxOverlapSize;
    }

    public List<TextSegment> split(Document document) {
        List<TextSegment> segments = new ArrayList<>();
        
        // 1. Gọi hàm đệ quy để cắt text thô thành các đoạn text nhỏ
        List<String> rawChunks = recursiveSplit(document.text(), separators[0], 0);

        // 2. Chuyển Metadata "nhà làm" sang Metadata "thư viện"
        Metadata libMetadata = new Metadata();
        document.metadata().asMap().forEach(libMetadata::put);

        // 3. Đóng gói thành TextSegment
        for (int i = 0; i < rawChunks.size(); i++) {
            // Clone metadata để mỗi segment có object riêng, tránh tham chiếu chéo
            Metadata segmentMeta = libMetadata.copy();
            segmentMeta.put("index", String.valueOf(i)); // Đánh số thứ tự
            
            segments.add(TextSegment.from(rawChunks.get(i), segmentMeta));
        }

        return segments;
    }

    // --- THUẬT TOÁN ĐỆ QUY (CORE LOGIC) ---
    
    private List<String> recursiveSplit(String text, String separator, int separatorIndex) {
        List<String> finalChunks = new ArrayList<>();

        // Nếu text đủ nhỏ rồi thì không cần cắt nữa, trả về luôn
        if (text.length() <= maxSegmentSize) {
            finalChunks.add(text);
            return finalChunks;
        }

        // Nếu đã hết các loại ký tự phân cách mà text vẫn to -> Buộc phải cắt cứng (substring)
        if (separatorIndex >= separators.length) {
            return splitHard(text); 
        }

        // Bắt đầu cắt theo separator hiện tại
        String[] parts = text.split(separator);
        List<String> buffer = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String part : parts) {
            // Nếu mảnh ghép này + mảnh đang giữ (currentChunk) vẫn nhỏ hơn giới hạn
            if (currentChunk.length() + part.length() + separator.length() <= maxSegmentSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append(separator); // Nối lại dấu phân cách đã bị mất khi split
                }
                currentChunk.append(part);
            } else {
                // Nếu ghép vào bị lố -> Lưu mảnh đang giữ lại
                if (currentChunk.length() > 0) {
                    buffer.add(currentChunk.toString());
                    currentChunk.setLength(0); // Reset
                }
                
                // Xử lý mảnh 'part' hiện tại
                if (part.length() > maxSegmentSize) {
                    // Nếu riêng mảnh này đã quá to -> Đệ quy tiếp với separator cấp thấp hơn
                    // Ví dụ: Đang cắt \n mà thấy to quá -> Cắt tiếp bằng dấu cách
                    int nextSeparatorIndex = separatorIndex + 1;
                    String nextSep = (nextSeparatorIndex < separators.length) ? separators[nextSeparatorIndex] : "";
                    buffer.addAll(recursiveSplit(part, nextSep, nextSeparatorIndex));
                } else {
                    currentChunk.append(part);
                }
            }
        }

        // Đừng quên mảnh cuối cùng còn sót lại trong StringBuilder
        if (currentChunk.length() > 0) {
            buffer.add(currentChunk.toString());
        }

        return buffer;
    }

    // Hàm dự phòng: Cắt cứng khi không còn dấu phân cách nào (ít khi xảy ra)
    private List<String> splitHard(String text) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += maxSegmentSize) {
            chunks.add(text.substring(i, Math.min(length, i + maxSegmentSize)));
        }
        return chunks;
    }
}