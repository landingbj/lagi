package ai.lagi.pojo;

public class QwenCompletionRequest {
    private String model;
    private QwenInput input;
    private QwenParameters parameters;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public QwenInput getInput() {
        return input;
    }

    public void setInput(QwenInput input) {
        this.input = input;
    }

    public QwenParameters getParameters() {
        return parameters;
    }

    public void setParameters(QwenParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "QwenCompletionRequest [model=" + model + ", input=" + input + ", parameters=" + parameters + "]";
    }
}
