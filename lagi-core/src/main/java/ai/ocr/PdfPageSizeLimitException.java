package ai.ocr;

public class PdfPageSizeLimitException extends Exception {
    public PdfPageSizeLimitException() {
        super("PDF page size limit exceeded");
    }

    public PdfPageSizeLimitException(String message) {
        super(message);
    }
}
