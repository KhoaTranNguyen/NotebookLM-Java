package knockbooklm.controller.base_splitter;

public class MyDocumentSplitters {
    
    // 1. Choose public static in order to be directly called outside the package without defining any MyDocumentSplitters object
    // 2. MyDocumentSplitters act like a spokesman, for the entire base_splitter package
    // 3. As a spokesman, its repsonsibility is to:
    //      (1) receive int maxChars and int maxOverlap
    //      (2) call the first "cutting method", MyParagraphSplitter
    public static MyDocumentSplitter recursive(int maxChars, int maxOverlap) {
        // 1. Create Russian dolls structure of splitters:
        // 2. Paragraph -> call Sentence -> call Word
        // 3. Replace the if .. else method, we just directly call the first splitter
        return new MyParagraphSplitter(maxChars, maxOverlap);
        /*
        But how the recursive()'s return-type, which are MyDocumentSplitter class, matched with MyParagraphSplitter class?
        - This is due to the reason MyDocumentSplitter is an interface
        - And MyParagraphSplitter "might be" a class which has "implemented" that interface.
            In fact, each sub-class is extends from the parent class, MyHierachicalSplitter, which
            (1) implemented MyDocumentSplitter interface
            (2) define the "core-logic" or split()
        - Therefore, this is one of the most efficient way to do the splitter
        - As MyDocumentSplitter interface only need to declare the undefined split() method
        - Each hierachy class, which are MyParagraphSplitter, MySentenceSplitter, MyWordSplitter, can implement their own split method
        */
    }
}
