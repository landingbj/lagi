package ai.llm.pojo;

public class QwenResponse {
    public QwenOutput output;
    public QwenUsage usage;
    public String request_id;

    public QwenOutput getOutput() {
        return output;
    }

    public void setOutput(QwenOutput output) {
        this.output = output;
    }

    public QwenUsage getUsage() {
        return usage;
    }

    public void setUsage(QwenUsage usage) {
        this.usage = usage;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    @Override
    public String toString() {
        return "QwenResponse [output=" + output + ", usage=" + usage + ", request_id=" + request_id + "]";
    }
}
