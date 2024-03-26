package ai.migrate.pojo;

public class GenerationsRequest {
    private String prompt;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "CopyOfCaptionRequest [prompt=" + prompt + "]";
    }
}
