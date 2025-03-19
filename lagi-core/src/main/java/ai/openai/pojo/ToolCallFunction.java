package ai.openai.pojo;

public class ToolCallFunction {
    private String name;
    private String arguments;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String value) {
        this.arguments = value;
    }

    @Override
    public String toString() {
        return "ToolCallFunction [name=" + name + ", arguments=" + arguments
                + "]";
    }

}
