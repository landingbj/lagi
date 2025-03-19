package ai.openai.pojo;

public class Tool {
    private String type;
    private Function function;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }


    public Function getFunction() {
        return function;
    }

    public void setFunction(Function value) {
        this.function = value;
    }

    @Override
    public String toString() {
        return "Tool [type=" + type + ", function=" + function + "]";
    }


}
