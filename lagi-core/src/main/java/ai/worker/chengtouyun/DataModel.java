package ai.worker.chengtouyun;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataModel {
    private String text;
    private String params;
    private String toolCalls;
    private Integer turns;

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("params")
    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @JsonProperty("tool_calls")
    public String getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(String toolCalls) {
        this.toolCalls = toolCalls;
    }

    @JsonProperty("turns")
    public Integer getTurns() {
        return turns;
    }

    public void setTurns(Integer turns) {
        this.turns = turns;
    }
}
