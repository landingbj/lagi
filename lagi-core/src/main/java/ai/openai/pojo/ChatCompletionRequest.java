package ai.openai.pojo;

import java.util.List;

public class ChatCompletionRequest {
    private String sessionId;
    private String model;
    private Double temperature;
    private Integer max_tokens;
    private String category;
    private List<ChatMessage> messages;
    private Boolean stream;
    private List<Tool> tools;
    private String tool_choice;
    private Boolean parallel_tool_calls;
    private Double presence_penalty;
    private Double frequency_penalty;
    private Double top_p;

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public String getTool_choice() {
        return tool_choice;
    }

    public void setTool_choice(String tool_choice) {
        this.tool_choice = tool_choice;
    }

    public Boolean getParallel_tool_calls() {
        return parallel_tool_calls;
    }

    public void setParallel_tool_calls(Boolean parallel_tool_calls) {
        this.parallel_tool_calls = parallel_tool_calls;
    }

    public Double getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(Double presence_penalty) {
        this.presence_penalty = presence_penalty;
    }

    public Double getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(Double frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }

    @Override
    public String toString() {
        return "ChatCompletionRequest [sessionId=" + sessionId + ", model=" + model + ", temperature=" + temperature
                + ", max_tokens=" + max_tokens + ", category=" + category + ", messages=" + messages + ", stream="
                + stream + ", tools=" + tools + ", tool_choice=" + tool_choice + ", parallel_tool_calls="
                + parallel_tool_calls + ", presence_penalty=" + presence_penalty + ", frequency_penalty="
                + frequency_penalty + ", top_p=" + top_p + "]";
    }
}
