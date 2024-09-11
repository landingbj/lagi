package ai.worker.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadFile {
    private String fileName;
    private String realName;
    private String filePath;
    private String text;
    private String translatedFilePath;
    private String translatedText;
}
