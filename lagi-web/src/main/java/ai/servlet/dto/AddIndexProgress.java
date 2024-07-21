package ai.servlet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddIndexProgress {
    private String filename;
    private int totalFileSize;
    private int processedFileSize;
}