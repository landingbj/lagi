package ai.worker.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenerateEssayRequest {
    private String emphasis;
    private List<UploadFile> uploadFileList;
    private String taskId;
}
