package com.khoa.notebooklm.desktop.controller.base_splitter;

import com.khoa.notebooklm.desktop.controller.base_class.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import java.util.ArrayList;
import java.util.List;

public abstract class MyHierarchicalSplitter implements MyDocumentSplitter {

    protected final int maxSegmentSize;
    protected final int maxOverlapSize;
    protected final MyDocumentSplitter subSplitter; // Subordinate Disciple

    protected MyHierarchicalSplitter(int maxSegmentSize, int maxOverlapSize, MyDocumentSplitter subSplitter) {
        this.maxSegmentSize = maxSegmentSize;
        this.maxOverlapSize = maxOverlapSize;
        // If no subSplitter is set -> use default
        this.subSplitter = (subSplitter == null) ? defaultSubSplitter() : subSplitter;
    }

    // --- DEFINE ABSTRACT MEMBERS (FUNCTIONS) ---
    protected abstract String[] splitText(String text); // Cut by? (\n\n hay . or space)?
    protected abstract String joinDelimiter();          // Rejoin by?
    protected abstract MyDocumentSplitter defaultSubSplitter(); // Who is default Subordinate Disciple?

    @Override
    public List<TextSegment> split(Document document) {
        List<TextSegment> segments = new ArrayList<>();

        // TỐI ƯU HÓA: Chỉ chuyển đổi metadata gốc MỘT LẦN
        Metadata baseMetadata = new Metadata();
        document.metadata().asMap().forEach(baseMetadata::put);
        
        // Read SegmentBuilder definition is in its own class's comment
        // segmentBuilder object is initialized as a temp-bucket for storing String segment
        // once the fill-in step in complete, it will be .reset() for a new String segment from document object
        SegmentBuilder segmentBuilder = new SegmentBuilder(maxSegmentSize, joinDelimiter());
        
        // 1. Cắt thô văn bản ra thành các mảnh (parts)
        String[] parts = splitText(document.text());
        String overlap = null;

        int index = 0;

        for (String part : parts) {
            // 2. Nếu xô còn chỗ -> Bỏ vào
            if (segmentBuilder.hasSpaceFor(part)) {
                segmentBuilder.append(part);
            } else {
                // 3. Xô đầy -> Đóng gói
                if (segmentBuilder.isNotEmpty()) {
                    String segmentText = segmentBuilder.toString();
                    
                    // Tránh tạo segment trùng lặp (do overlap)
                    if (!segmentText.equals(overlap)) {
                        // segments is List<TextSegment> type (matched with Langchain4j)
                        segments.add(createTextSegment(segmentText, baseMetadata, index++));
                        
                        // Tính toán Overlap cho xô tiếp theo
                        overlap = getOverlapFrom(segmentText);
                        
                        // Reset xô và bỏ phần overlap vào trước
                        segmentBuilder.reset();
                        segmentBuilder.append(overlap);
                        
                        // Thử bỏ lại 'part' vào xô mới (đã có overlap)
                        if (segmentBuilder.hasSpaceFor(part)) {
                            segmentBuilder.append(part);
                            continue; // Done, qua vòng lặp tiếp
                        }
                    }
                }

                // 4. XỬ LÝ CỤC ĐÁ TẢNG (Nếu 1 mình thằng 'part' đã to hơn cả cái xô)
                if (subSplitter == null) {
                    // Nếu không có đệ tử -> Cắt cứng (Hard split) hoặc báo lỗi
                    // Ở đây mình chọn cắt cứng cho an toàn thay vì throw Exception
                    segments.add(createTextSegment(part, baseMetadata, index++)); 
                } else {
                    // Gọi đệ tử xử lý cục đá này
                    // Lưu ý: Đệ tử cũng phải trả về TextSegment
                    // Document subDoc = Document.from(segmentBuilder.toString() + part, document.metadata()); 
                    // (Chỗ này trick một xíu: ta ghép buffer cũ + part quá khổ để đưa đệ tử xử lý hết)
                    // LÝ DO KHÔNG SỬ DỤNG (VÌ SAO CÁCH NÀY RỦI RO):
                    // Vấn đề cốt lõi là ký tự/chuỗi phân tách (delimiter) giữa hai `part` đã bị
                    // `text.split()` "tiêu thụ" mất.
                    // 1. Ghép trực tiếp `overlap + part` sẽ tạo ra chuỗi sai cú pháp (ví dụ: "word1word2").
                    // 2. Chèn lại `joinDelimiter()` ở giữa (`overlap + joinDelimiter() + part`) cũng không
                    //    an toàn, vì ta không thể biết chính xác chuỗi phân tách ban đầu là gì
                    //    (ví dụ: `split` bằng `\s+` có thể "ăn" nhiều dấu cách/xuống dòng, trong khi
                    //    `joinDelimiter` chỉ là một dấu cách).
                    // -> Vì vậy, giải pháp an toàn hơn là chỉ split `part` (cách đang chạy), dù phải
                    //    chấp nhận chuỗi overlap bị gián đoạn trong trường hợp đặc biệt này.
                    
                    List<TextSegment> subSegments = subSplitter.split(Document.from(part, document.metadata()));
                    for (TextSegment subSeg : subSegments) {
                        segments.add(createTextSegment(subSeg.text(), baseMetadata, index++));
                    }
                }
                
                // Reset lại buffer sau khi xử lý cục đá
                String lastText = segments.get(segments.size()-1).text();
                overlap = getOverlapFrom(lastText);
                segmentBuilder.reset();
                segmentBuilder.append(overlap);
            }
        }

        // 5. Đừng quên vét nốt những gì còn sót lại trong xô
        if (segmentBuilder.isNotEmpty() && !segmentBuilder.toString().equals(overlap)) {
            segments.add(createTextSegment(segmentBuilder.toString(), baseMetadata, index));
        }

        return segments;
    }

    // Logic lấy Overlap đơn giản: Lấy maxOverlapSize ký tự cuối cùng
    private String getOverlapFrom(String text) {
        if (maxOverlapSize == 0 || text.isEmpty()) return "";
        if (text.length() <= maxOverlapSize) return text;
        return text.substring(text.length() - maxOverlapSize);
    }

    private TextSegment createTextSegment(String text, Metadata baseMetadata, int index) {
        // At this moment, baseMetadata is pass-by-value, but it is also a reference value by default
        // So, it is necessary to use .copy() to duplicate its value -> segmentMetadata
        Metadata segmentMetadata = baseMetadata.copy();
        segmentMetadata.put("index", String.valueOf(index));

        return TextSegment.from(text, segmentMetadata);
    }
}
