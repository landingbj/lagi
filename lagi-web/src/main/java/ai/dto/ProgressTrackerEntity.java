package ai.dto;

public class ProgressTrackerEntity {
    private String taskId;
    private int progress; // 进度（0-100）

    public ProgressTrackerEntity(String taskId) {
        this.taskId = taskId;
        this.progress = 0;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
