package ai.worker.zhipu;

public enum StepInfoEnum {
    DONE(0, "完成"),
    FAIL(-1, "失败"),
    START(1, "任务开始"),
    TRANSLATE(2, "文章翻译完成"),
    ADD_VECTORS(3, "向量数据库写入成功"),
    TOPIC(4, "提取主题成功"),
    OUTLINE(5, "生成纲要成功"),
    CHAPTER(6, "生成每个章节成功");

    private final Integer status;
    private final String message;

    StepInfoEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
