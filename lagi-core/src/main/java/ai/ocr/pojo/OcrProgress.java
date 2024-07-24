package ai.ocr.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrProgress {
    private String md5;
    private String filename;
    private int totalPageSize;
    private int processedPageSize;
    private int totalFileSize;
    private int processedFileSize;
}