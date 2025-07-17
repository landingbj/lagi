package ai.workflow.pojo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * TaskCancelOutput - 任务取消输出结果
 * 对应TypeScript的 TaskCancelOutput 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCancelOutput {
    private boolean success; // 表示任务是否成功取消
} 