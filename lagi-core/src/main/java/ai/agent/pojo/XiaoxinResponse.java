package ai.agent.pojo;

import lombok.Data;

import java.util.List;

@Data
public class XiaoxinResponse {
    private long time;
    private DataContent data;
    private int code;
    private String msg;

    @Data
    public static class DataContent {
        private boolean flowOutput;
        private String queryId;
        private String sessionId;
        private String source;
        private boolean solved;
        private double confidence;
        private Answer answer;
        private String queryTime;
        private String answerTime;
        private List<Object> actions;
        private boolean webhook;
        private boolean topAnythingElse;
        private boolean anythingElse;
        private String dialogSceneStatus;
        private boolean showSource;
    }

    @Data
    public static class Answer {
        private String answerText;
        private List<AnswerContent> answerContents;
        private List<Object> recommendContents;
    }

    @Data
    public static class AnswerContent {
        private String text;
        private int type;
    }

    @Data
    public static class Action {
        private String query;
        private String prob;
        private String source;
        private String name;
        private String nameZh;
    }
}
