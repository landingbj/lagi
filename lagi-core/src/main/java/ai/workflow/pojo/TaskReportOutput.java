package ai.workflow.pojo;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * TaskReportOutput - 任务报告输出
 * 对应TypeScript的 TaskReportOutput = IReport | undefined
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportOutput {
    private String id;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private WorkflowStatus workflowStatus;
    private Map<String, NodeReport> reports;
    private WorkflowMessages messages;

    // 内部类定义
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStatus {
        private String status; // "pending", "processing", "succeeded", "failed", "canceled"
        private boolean terminated;
        private Long startTime;
        private Long endTime;
        private Long timeCost;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeReport extends WorkflowStatus {
        private String id;
        private List<Snapshot> snapshots;
        
        @Override
        public String toString() {
            return "NodeReport{" +
                    "id='" + id + '\'' +
                    ", snapshots=" + snapshots +
                    ", status='" + getStatus() + '\'' +
                    ", terminated=" + isTerminated() +
                    ", startTime=" + getStartTime() +
                    ", endTime=" + getEndTime() +
                    ", timeCost=" + getTimeCost() +
                    '}';
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Snapshot {
        private String id;
        private String nodeID;
        private Map<String, Object> inputs;
        private Map<String, Object> outputs;
        private Object data;
        private String branch;
        private String error;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowMessages {
        private List<Message> log;
        private List<Message> info;
        private List<Message> debug;
        private List<Message> error;
        private List<Message> warning;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String id;
        private String type; // "log", "info", "debug", "error", "warning"
        private String message;
        private String nodeID;
        private Long timestamp;
    }
}