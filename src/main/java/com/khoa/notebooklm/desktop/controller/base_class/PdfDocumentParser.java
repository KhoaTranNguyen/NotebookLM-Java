package com.khoa.notebooklm.desktop.controller.base_class;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import dev.langchain4j.data.document.BlankDocumentException;

import java.io.IOException;
import java.io.InputStream;

public class PdfDocumentParser {

   public PdfDocumentParser() {}

   public Document parse(InputStream inputStream) {
        return parse(inputStream, null); // fileName = null
    }

   public Document parse(InputStream inputStream, String fileName) {
        // Try-with-resources: auto close PDF after done (even bug)
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {

            // extract text
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            
            if (text == null || text.isBlank()) {
                throw new BlankDocumentException();
            }

            // auto get metadata (empty if does not have)
            Metadata metadata = this.toMetadata(pdfDocument);
            
            // get fileName
            // if fileName null -> FILE_NAME entry will not exist in metadata
            if (fileName != null && !fileName.isBlank()) {
                // use FILE_NAME const at Document class to avoid wrong typing
                metadata.put(Document.FILE_NAME, fileName);
            }

            // Trả về Document gồm cả Text và Metadata (có thể rỗng nhưng không null)
            return Document.from(text, metadata);

        } catch (IOException e) {
            throw new RuntimeException("Error when getting PDF: " + e.getMessage(), e);
        }
    }

   private Metadata toMetadata(PDDocument pdDocument) {
      // access Document Information Dictionary (at Trailer) of PDF
      PDDocumentInformation documentInformation = pdDocument.getDocumentInformation();
      Metadata metadata = new Metadata();
      
      // .getMetadataKeys() scan the DID and extract all keys, whether they're custom or not
      // Default: ("Author", "Title", "Creator", "Subject"...)
      // Custom: ("CompanyID", "ScanSource", "BaoMat"...)
      documentInformation.getMetadataKeys().forEach(key -> {
         // getCustomMetadataValue() works like a Generic Getter
         // the key's value can be formatted in many ways like (COSString, COSName, etc.)
         // this function auto type-casting and decode all that types -> String (null or not)
         String value = documentInformation.getCustomMetadataValue(key);
         if (value != null) {
               metadata.put(key, value);
         }
      });

      /* The above code is simplified and more advanced version of:

         // 1. Get keys list
         Set<String> keys = documentInformation.getMetadataKeys();

         // 2. Scan each key
         for (String key : keys) {
            String value = documentInformation.getCustomMetadataValue(key);
            
            // 3. Check null and put in metadata
            if (value != null) {
               metadata.put(key, value);
            }
         }
      
      */

      return metadata;
   }
}
