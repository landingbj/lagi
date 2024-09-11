package ai.worker.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StepInfo {
    private String taskId;
    private Integer status;
    private String message;
}
