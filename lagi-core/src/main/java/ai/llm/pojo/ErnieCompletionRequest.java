package ai.llm.pojo;

import java.util.List;

public class ErnieCompletionRequest {
    private List<ErnieMessage> messages;
    private Boolean stream;
    private Double temperature;
    private Double top_p;
    private Double penalty_score;
    private Double system;
    private Double user_id;

    public List<ErnieMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ErnieMessage> messages) {
        this.messages = messages;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }

    public Double getPenalty_score() {
        return penalty_score;
    }

    public void setPenalty_score(Double penalty_score) {
        this.penalty_score = penalty_score;
    }

    public Double getSystem() {
        return system;
    }

    public void setSystem(Double system) {
        this.system = system;
    }

    public Double getUser_id() {
        return user_id;
    }

    public void setUser_id(Double user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "ErnieCompletionRequest [messages=" + messages + ", stream=" + stream + ", temperature=" + temperature
                + ", top_p=" + top_p + ", penalty_score=" + penalty_score + ", system=" + system + ", user_id="
                + user_id + "]";
    }
}
