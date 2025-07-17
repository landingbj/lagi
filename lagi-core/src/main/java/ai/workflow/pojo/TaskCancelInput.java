package ai.workflow.pojo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * TaskCancelInput - 任务取消输入参数
 * 对应TypeScript的 TaskCancelInput 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCancelInput {
    private String taskID; // 任务的唯一标识符，由 TaskRun API 返回
} 