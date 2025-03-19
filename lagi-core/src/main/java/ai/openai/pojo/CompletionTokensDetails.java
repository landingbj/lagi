package ai.openai.pojo;

public class CompletionTokensDetails {
    private long reasoning_tokens;

    public long getReasoning_tokens() {
        return reasoning_tokens;
    }

    public void setReasoning_tokens(long reasoning_tokens) {
        this.reasoning_tokens = reasoning_tokens;
    }

    @Override
    public String toString() {
        return "CompletionTokensDetails [reasoning_tokens=" + reasoning_tokens + "]";
    }
}
