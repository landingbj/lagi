package ai.openai.pojo;

public class ToolCall {
    private String id;
    private String type;
    private ToolCallFunction function;

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public ToolCallFunction getFunction() {
        return function;
    }

    public void setFunction(ToolCallFunction value) {
        this.function = value;
    }

    @Override
    public String toString() {
        return "ToolCall [id=" + id + ", type=" + type + ", function="
                + function + "]";
    }

}
