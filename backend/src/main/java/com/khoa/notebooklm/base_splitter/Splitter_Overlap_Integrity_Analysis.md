# Phân tích: Xử lý "Cục đá tảng" và tính toàn vẹn của Overlap trong MyHierarchicalSplitter

Tài liệu này tổng hợp lại quá trình phân tích một tình huống xử lý cạnh (edge case) trong logic của `MyHierarchicalSplitter`: làm thế nào để xử lý một "cục đá tảng" (một `part` văn bản lớn hơn cả `maxSegmentSize`) và những ảnh hưởng của nó đến tính toàn vẹn của chuỗi ngữ cảnh (overlap chain).

## 1. Bối cảnh: Vấn đề "Cục đá tảng"

Trong `MyHierarchicalSplitter`, văn bản được chia thành các `part` nhỏ hơn. Một vòng lặp sẽ gom các `part` này vào `SegmentBuilder` cho đến khi đầy.

Tuy nhiên, vấn đề xảy ra khi một `part` đơn lẻ lại lớn hơn cả kích thước tối đa của một segment (`maxSegmentSize`). Chúng ta gọi đây là "cục đá tảng". Khi đó, logic sẽ cố gắng ủy quyền việc chia nhỏ "cục đá tảng" này cho một `subSplitter` (đệ tử) ở cấp độ tinh hơn (ví dụ: Paragraph -> Sentence).

Trong mã nguồn, có hai hướng tiếp cận được đặt ra để xử lý vấn đề này.

## 2. Hai hướng tiếp cận

### Hướng 1: Cách tiếp cận đơn giản (Đang hoạt động)

Đoạn mã đang chạy thực hiện một logic đơn giản và an toàn:

```java
List<TextSegment> subSegments = subSplitter.split(Document.from(part, document.metadata()));
```

- **Logic:** Chỉ đưa `part` ("cục đá tảng") cho `subSplitter` xử lý.
- **Vấn đề:** Cách này **làm đứt chuỗi ngữ cảnh (overlap chain)**. Segment cuối cùng trước "cục đá tảng" có một đoạn `overlap`, nhưng segment đầu tiên được tạo ra từ "cục đá tảng" lại không bắt đầu bằng `overlap` đó. Điều này tạo ra một "khoảng trống" ngữ cảnh, có thể làm giảm chất lượng kết quả từ AI.

### Hướng 2: "Trick" (Đã được comment ra)

Một ý tưởng khác đã được cân nhắc và ghi chú lại trong comment:

```java
// Document subDoc = Document.from(segmentBuilder.toString() + part, document.metadata());
```

- **Logic:** Ghép nối `overlap` hiện có trong `segmentBuilder` với `part` ("cục đá tảng") và đưa cả chuỗi lớn đó cho `subSplitter` xử lý.
- **Mục tiêu:** Đây là một nỗ lực để **sửa lỗi "đứt chuỗi ngữ cảnh"**. Bằng cách này, segment đầu tiên do `subSplitter` tạo ra sẽ bắt đầu bằng `overlap` của segment trước, đảm bảo chuỗi ngữ cảnh được liền mạch.

## 3. Vấn đề cốt lõi: `joinDelimiter` bị mất

Thoạt nhìn, "trick" có vẻ là giải pháp đúng đắn. Tuy nhiên, nó lại vấp phải một vấn đề kỹ thuật tinh vi, đó là **chuỗi phân tách (delimiter) đã bị mất khi `split()`**.

Hàm `text.split(regex)` hoạt động bằng cách tìm và "tiêu thụ" delimiter, trả về các phần còn lại. Điều này có nghĩa là ký tự nối giữa `part` trước và `part` sau đã biến mất.

**Ví dụ với `MyWordSplitter`:**
- Văn bản gốc: `"word1 word2"`
- `split(" ")` tạo ra: `part1` = `"word1"` và `part2` = `"word2"`. Dấu cách đã bị mất.
- Nếu `overlap` là `"word1"`, thì `overlap + part2` sẽ trở thành **`"word1word2"`**.

Chuỗi `"word1word2"` này bị sai cú pháp và có thể làm hỏng logic của `subSplitter`.

## 4. Tại sao không chỉ chèn lại `joinDelimiter`?

Một giải pháp có vẻ hợp lý là chèn lại `joinDelimiter`: `overlap + joinDelimiter() + part`.

Tuy nhiên, cách này vẫn không đáng tin cậy vì **regex dùng để `split` và chuỗi `joinDelimiter` không đối xứng 1-1**.
- `MySentenceSplitter` dùng `split("...\\s+...")` để "ăn" một hoặc nhiều ký tự khoảng trắng (`\s+`).
- Nhưng `joinDelimiter()` của nó chỉ là một dấu cách `" "`.

Chúng ta không thể biết chính xác chuỗi phân tách ban đầu là gì (`" "`, `"\n"`, hay `"  "`) để tái tạo lại một cách hoàn hảo. Việc chỉ chèn `joinDelimiter()` là một phỏng đoán và có thể không đúng.

## 5. Kết luận

Người lập trình đã phải đứng trước một lựa chọn khó khăn giữa hai giải pháp không hoàn hảo:

1.  **An toàn (Cách 1):** Chấp nhận làm đứt chuỗi ngữ cảnh, nhưng đảm bảo `subSplitter` luôn nhận được đầu vào sạch sẽ và hoạt động đáng tin cậy.
2.  **Mạo hiểm (Cách 2):** Cố gắng duy trì chuỗi ngữ cảnh, nhưng có nguy cơ tạo ra văn bản sai cú pháp và làm `subSplitter` chạy lỗi.

Việc họ chọn cách tiếp cận an toàn hơn và comment lại "trick" là một quyết định kỹ thuật thực dụng. Nó ưu tiên sự ổn định và dễ đoán của thuật toán hơn là một sự "thông minh" có thể gây ra lỗi khó lường. Phân tích này cho thấy một thách thức tinh vi nhưng quan trọng trong các thuật toán chia văn bản.
