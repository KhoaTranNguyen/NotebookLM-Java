package com.khoa.notebooklm.desktop.controller.base_splitter;

class SegmentBuilder {
    // 1. SegmentBuilder efficiently uses the strength of "StringBuilder" build-in final data-type
    //      (1) StringBuilder is extremely versatile for it owns methods: append(), setLength(), toString()
    //      (2) StringBuilder is designed for modifying the String on 1 and only memory space,
    //          not like when using String ("a" + "b") will create additional memory space

    private final StringBuilder sb = new StringBuilder();
    private final int maxChars;
    private final String joinDelimiter;

    public SegmentBuilder(int maxChars, String joinDelimiter) {
        this.maxChars = maxChars;
        this.joinDelimiter = joinDelimiter;
    }

    // 2. hasSpaceFor is the self-defined function of SegmentBuilder
    //      in order to check the availability of Segment
    public boolean hasSpaceFor(String text) {
        int currentLen = sb.length();
        int newLen = text.length();
        // Nếu xô đang có đồ, phải tính thêm cả kích thước của dấu nối (ví dụ khoảng trắng)
        int delimiterLen = (currentLen > 0) ? joinDelimiter.length() : 0;
        return (currentLen + delimiterLen + newLen) <= maxChars;
    }

    // 3. Also, void append() is SegmentBuilder's self-defined, not to be confused with StringBuilder's append()
    //      In this method, if will check whether the sb (StringBuilder) already contains text
    //      (1) If yes, if will append joinDelimiter first
    //      (2) then, text
    public void append(String text) {
        if (sb.length() > 0) {
            sb.append(joinDelimiter);
        }
        sb.append(text);
    }

    public void reset() {
        sb.setLength(0);
    }

    public String toString() {
        return sb.toString();
    }

    public boolean isNotEmpty() {
        return sb.length() > 0;
    }
}
