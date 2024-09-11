package ai.worker.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenerateEssayData {
    String topic;
    String outline;
    List<String> chapterList;
    List<UploadFile> uploadFileList;
}
