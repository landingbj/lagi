package ai.workflow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作流执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResult {
    private boolean success;
    private Object result;
    private String errorMessage;
    private List<WorkflowResult> subResults;
    
    public WorkflowResult(boolean success, Object result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
        this.subResults = null;
    }
}