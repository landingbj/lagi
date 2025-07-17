package ai.workflow;

import ai.common.utils.LRUCache;
import ai.workflow.pojo.TaskReportOutput;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 任务状态管理器 - 线程安全的任务状态管理
 * 负责管理任务的生命周期状态，包括创建、查询、更新和取消操作
 */
@Slf4j
public class TaskStatusManager {
    private static final TaskStatusManager INSTANCE = new TaskStatusManager();

    // 线程安全的LRU缓存，用于存储任务报告
    private final LRUCache<String, TaskReportOutput> taskReportCache;

    // 读写锁，确保线程安全
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    // 任务状态变更监听器
    private final Map<String, List<TaskStatusListener>> statusListeners = new ConcurrentHashMap<>();

    private TaskStatusManager() {
        this.taskReportCache = new LRUCache<>(10000);
    }

    /**
     * 获取单例实例
     */
    public static TaskStatusManager getInstance() {
        return INSTANCE;
    }

    /**
     * 创建新任务
     */
    public String createTask(TaskReportOutput taskReport) {
        if (taskReport == null || taskReport.getId() == null) {
            throw new IllegalArgumentException("Task report and ID cannot be null");
        }

        String taskId = taskReport.getId();

        cacheLock.writeLock().lock();
        try {
            taskReportCache.put(taskId, taskReport);
            log.info("Task created: {}", taskId);
            notifyStatusChange(taskId, "created", taskReport);
            return taskId;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 获取任务报告
     */
    public TaskReportOutput getTaskReport(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return null;
        }

        cacheLock.readLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport != null && taskReport.getWorkflowStatus() != null) {
                // 更新结束时间和耗时
                updateTaskTiming(taskReport);
            }
            return taskReport;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 检查任务是否存在
     */
    public boolean taskExists(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return false;
        }

        cacheLock.readLock().lock();
        try {
            return taskReportCache.get(taskId) != null;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 检查任务是否已完成
     */
    public boolean isTaskCompleted(String taskId) {
        TaskReportOutput taskReport = getTaskReport(taskId);
        if (taskReport == null || taskReport.getWorkflowStatus() == null) {
            return false;
        }
        String status = taskReport.getWorkflowStatus().getStatus();
        return "succeeded".equals(status) || "failed".equals(status);
    }

    /**
     * 检查任务是否已取消
     */
    public boolean isTaskCanceled(String taskId) {
        TaskReportOutput taskReport = getTaskReport(taskId);
        if (taskReport == null || taskReport.getWorkflowStatus() == null) {
            return false;
        }
        String status = taskReport.getWorkflowStatus().getStatus();
        return "canceled".equals(status);
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return false;
        }

        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport == null) {
                log.warn("Task not found for cancellation: {}", taskId);
                return false;
            }

            // 检查任务状态
            if (isTaskCompleted(taskId)) {
                log.warn("Cannot cancel completed task: {}", taskId);
                return false;
            }

            if (isTaskCanceled(taskId)) {
                log.warn("Task already canceled: {}", taskId);
                return false;
            }

            // 更新工作流状态为已取消
            TaskReportOutput.WorkflowStatus workflowStatus = taskReport.getWorkflowStatus();
            if (workflowStatus != null) {
                workflowStatus.setStatus("canceled");
                workflowStatus.setTerminated(true);
                workflowStatus.setEndTime(System.currentTimeMillis());
                if (workflowStatus.getStartTime() > 0) {
                    workflowStatus.setTimeCost(workflowStatus.getEndTime() - workflowStatus.getStartTime());
                }
            }

            // 更新所有节点的状态为已取消
            if (taskReport.getReports() != null) {
                for (TaskReportOutput.NodeReport nodeReport : taskReport.getReports().values()) {
                    if (nodeReport.getStatus().equals("pending") || nodeReport.getStatus().equals("processing")) {
                        nodeReport.setStatus("canceled");
                        nodeReport.setTerminated(true);
                        nodeReport.setEndTime(System.currentTimeMillis());
                        if (nodeReport.getStartTime() > 0) {
                            nodeReport.setTimeCost(nodeReport.getEndTime() - nodeReport.getStartTime());
                        }
                    }
                }
            }

            // 添加取消消息到工作流消息中
            addCancelMessage(taskReport);
            // 更新缓存
            taskReportCache.put(taskId, taskReport);

            log.info("Task canceled successfully: {}", taskId);
            notifyStatusChange(taskId, "canceled", taskReport);
            return true;

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 更新任务状态
     */
    public boolean updateTaskStatus(String taskId, String status) {
        if (taskId == null || taskId.trim().isEmpty() || status == null) {
            return false;
        }

        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport == null) {
                return false;
            }

            TaskReportOutput.WorkflowStatus workflowStatus = taskReport.getWorkflowStatus();
            if (workflowStatus != null) {
                workflowStatus.setStatus(status);
                if ("succeeded".equals(status) || "failed".equals(status) || "canceled".equals(status)) {
                    workflowStatus.setTerminated(true);
                    workflowStatus.setEndTime(System.currentTimeMillis());
                    if (workflowStatus.getStartTime() > 0) {
                        workflowStatus.setTimeCost(workflowStatus.getEndTime() - workflowStatus.getStartTime());
                    }
                }
            }

            taskReportCache.put(taskId, taskReport);
            log.info("Task status updated: {} -> {}", taskId, status);
            notifyStatusChange(taskId, status, taskReport);
            return true;

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 更新节点状态
     */
    public boolean updateNodeStatus(String taskId, String nodeId, String status) {
        if (taskId == null || nodeId == null || status == null) {
            return false;
        }

        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport == null || taskReport.getReports() == null) {
                return false;
            }

            TaskReportOutput.NodeReport nodeReport = taskReport.getReports().get(nodeId);
            if (nodeReport != null) {
                nodeReport.setStatus(status);
                if ("succeeded".equals(status) || "failed".equals(status) || "canceled".equals(status)) {
                    nodeReport.setTerminated(true);
                    nodeReport.setEndTime(System.currentTimeMillis());
                    if (nodeReport.getStartTime() > 0) {
                        nodeReport.setTimeCost(nodeReport.getEndTime() - nodeReport.getStartTime());
                    }
                }

                taskReportCache.put(taskId, taskReport);
                log.info("Node status updated: {} -> {} -> {}", taskId, nodeId, status);
                return true;
            }

            return false;

        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 添加取消消息
     */
    private void addCancelMessage(TaskReportOutput taskReport) {
        if (taskReport.getMessages() == null) {
            taskReport.setMessages(new TaskReportOutput.WorkflowMessages());
        }

        if (taskReport.getMessages().getInfo() == null) {
            taskReport.getMessages().setInfo(new ArrayList<>());
        }

        TaskReportOutput.Message cancelMessage = new TaskReportOutput.Message();
        cancelMessage.setId("msg_cancel_" + System.currentTimeMillis());
        cancelMessage.setType("info");
        cancelMessage.setMessage("任务已被用户取消");
        cancelMessage.setNodeID("system");
        cancelMessage.setTimestamp(System.currentTimeMillis());
        taskReport.getMessages().getInfo().add(cancelMessage);
    }

    /**
     * 更新任务时间信息
     */
    private void updateTaskTiming(TaskReportOutput taskReport) {
        TaskReportOutput.WorkflowStatus workflowStatus = taskReport.getWorkflowStatus();
        if (workflowStatus != null && workflowStatus.getStartTime() > 0) {
            long currentTime = System.currentTimeMillis();

            // 如果任务还未结束，更新结束时间和耗时
            if (!workflowStatus.isTerminated() || workflowStatus.getEndTime() == null) {
                workflowStatus.setEndTime(currentTime);
                workflowStatus.setTimeCost(currentTime - workflowStatus.getStartTime());
            }

            // 更新所有节点的结束时间和耗时
            if (taskReport.getReports() != null) {
                for (TaskReportOutput.NodeReport nodeReport : taskReport.getReports().values()) {
                    if (nodeReport.getStartTime() > 0 &&
                            (!nodeReport.isTerminated() || nodeReport.getEndTime() == null)) {
                        nodeReport.setEndTime(currentTime);
                        nodeReport.setTimeCost(currentTime - nodeReport.getStartTime());
                    }
                }
            }
        }
    }

    /**
     * 创建节点快照
     */
    public TaskReportOutput.Snapshot createNodeSnapshot(String nodeId, Map<String, Object> inputs,
                                                        Map<String, Object> outputs, String branch, String error) {
        TaskReportOutput.Snapshot snapshot = new TaskReportOutput.Snapshot();
        snapshot.setId("snapshot_" + nodeId + "_" + System.currentTimeMillis());
        snapshot.setNodeID(nodeId);
        
        if (inputs != null) {
            snapshot.setInputs(new ConcurrentHashMap<>(inputs));
        } else {
            snapshot.setInputs(new ConcurrentHashMap<>());
        }
        
        if (outputs != null) {
            snapshot.setOutputs(new ConcurrentHashMap<>(outputs));
        } else {
            snapshot.setOutputs(new ConcurrentHashMap<>());
        }
        
        snapshot.setData(null);
        snapshot.setBranch(branch);
        snapshot.setError(error);
        return snapshot;
    }

    /**
     * 更新节点报告
     */
    public void updateNodeReport(String taskId, String nodeId, String status,
                                 Long startTime, Long endTime, Long timeCost, TaskReportOutput.Snapshot snapshot) {
        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport != null && taskReport.getReports() != null) {
                TaskReportOutput.NodeReport nodeReport = taskReport.getReports().get(nodeId);
                if (nodeReport == null) {
                    nodeReport = new TaskReportOutput.NodeReport();
                    nodeReport.setId(nodeId);
                    nodeReport.setSnapshots(new ArrayList<>());
                }

                nodeReport.setStatus(status);
                nodeReport.setTerminated("succeeded".equals(status) || "failed".equals(status));
                nodeReport.setStartTime(startTime);
                nodeReport.setEndTime(endTime);
                nodeReport.setTimeCost(timeCost);

                // 添加快照
                if (nodeReport.getSnapshots() == null) {
                    nodeReport.setSnapshots(new ArrayList<>());
                }
                if (snapshot != null) {
                    nodeReport.getSnapshots().add(snapshot);
                }
                // 更新报告
                taskReport.getReports().put(nodeId, nodeReport);
                // 更新缓存
                taskReportCache.put(taskId, taskReport);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 添加执行日志
     */
    public void addExecutionLog(String taskId, String nodeId, String message, long timestamp) {
        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport != null) {
                if (taskReport.getMessages() == null) {
                    taskReport.setMessages(new TaskReportOutput.WorkflowMessages());
                }

                if (taskReport.getMessages().getLog() == null) {
                    taskReport.getMessages().setLog(new ArrayList<>());
                }

                TaskReportOutput.Message logMessage = new TaskReportOutput.Message();
                logMessage.setId("msg_" + nodeId + "_" + System.currentTimeMillis());
                logMessage.setType("log");
                logMessage.setMessage(message);
                logMessage.setNodeID(nodeId);
                logMessage.setTimestamp(timestamp);

                taskReport.getMessages().getLog().add(logMessage);

                // 更新缓存
                taskReportCache.put(taskId, taskReport);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 添加错误日志
     */
    public void addErrorLog(String taskId, String nodeId, String errorMessage, long timestamp) {
        cacheLock.writeLock().lock();
        try {
            TaskReportOutput taskReport = taskReportCache.get(taskId);
            if (taskReport != null) {
                if (taskReport.getMessages() == null) {
                    taskReport.setMessages(new TaskReportOutput.WorkflowMessages());
                }

                if (taskReport.getMessages().getError() == null) {
                    taskReport.getMessages().setError(new ArrayList<>());
                }

                TaskReportOutput.Message errorMessageObj = new TaskReportOutput.Message();
                errorMessageObj.setId("error_" + nodeId + "_" + System.currentTimeMillis());
                errorMessageObj.setType("error");
                errorMessageObj.setMessage(errorMessage);
                errorMessageObj.setNodeID(nodeId);
                errorMessageObj.setTimestamp(timestamp);

                taskReport.getMessages().getError().add(errorMessageObj);

                // 更新缓存
                taskReportCache.put(taskId, taskReport);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 创建初始任务报告
     */
    public TaskReportOutput createInitialTaskReport(String taskId, String workflowJson, Map<String, Object> inputData) {
        TaskReportOutput taskReport = new TaskReportOutput();

        taskReport.setId(taskId);
        taskReport.setInputs(inputData != null ? new ConcurrentHashMap<>(inputData) : new ConcurrentHashMap<>());
        taskReport.setOutputs(new ConcurrentHashMap<>());

        TaskReportOutput.WorkflowStatus workflowStatus = new TaskReportOutput.WorkflowStatus();
        workflowStatus.setStatus("processing");
        workflowStatus.setTerminated(false);
        workflowStatus.setStartTime(System.currentTimeMillis());
        workflowStatus.setEndTime(null);
        workflowStatus.setTimeCost(0L);
        taskReport.setWorkflowStatus(workflowStatus);

        taskReport.setReports(new ConcurrentHashMap<>());
        TaskReportOutput.WorkflowMessages messages = new TaskReportOutput.WorkflowMessages();
        messages.setLog(new ArrayList<>());
        messages.setInfo(new ArrayList<>());
        messages.setDebug(new ArrayList<>());
        messages.setError(new ArrayList<>());
        messages.setWarning(new ArrayList<>());

        TaskReportOutput.Message startMessage = new TaskReportOutput.Message();
        startMessage.setId("msg_start_" + System.currentTimeMillis());
        startMessage.setType("info");
        startMessage.setMessage("工作流开始执行");
        startMessage.setNodeID("system");
        startMessage.setTimestamp(System.currentTimeMillis());
        messages.getInfo().add(startMessage);
        taskReport.setMessages(messages);

        return taskReport;
    }

    /**
     * 通知状态变更
     */
    private void notifyStatusChange(String taskId, String status, TaskReportOutput taskReport) {
        List<TaskStatusListener> listeners = statusListeners.get(taskId);
        if (listeners != null) {
            for (TaskStatusListener listener : listeners) {
                try {
                    listener.onStatusChange(taskId, status, taskReport);
                } catch (Exception e) {
                    log.error("Error notifying status listener for task: {}", taskId, e);
                }
            }
        }
    }

    /**
     * 任务状态监听器接口
     */
    public interface TaskStatusListener {
        void onStatusChange(String taskId, String status, TaskReportOutput taskReport);
    }
} 