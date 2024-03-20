package ai.embedding.pojo;

import java.util.List;

public class OpenAIEmbeddingRequest {
    private List<String> input;
    private String model;

    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
