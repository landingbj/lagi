package ai.workflow.utils;

import ai.workflow.TaskStatusManager;
import ai.workflow.pojo.TaskReportOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeExecutorUtil {
    private static final TaskStatusManager taskStatusManager = TaskStatusManager.getInstance();

    public static void sleep() {
//        sleep(1, 3);
    }

    public static void sleep(int min, int max) {
        if (min == 0 && max == 0) {
            return;
        }
        try {
            Thread.sleep((min + (int) (Math.random() * (max - min))) * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    public static void handleException(String taskId, String nodeId, long startTime, String executorName, Exception e) throws Exception {
        long endTime = System.currentTimeMillis();
        long timeCost = endTime - startTime;
        TaskReportOutput.Snapshot snapshot = taskStatusManager.createNodeSnapshot(nodeId, null, null, null,e.getMessage());
        taskStatusManager.updateNodeReport(taskId, nodeId, "failed", startTime, endTime, timeCost, snapshot);
        taskStatusManager.addErrorLog(taskId, nodeId, executorName + "执行失败: " + e.getMessage(), endTime);
        log.error(e.getMessage(), e);
        throw e;
    }
}
