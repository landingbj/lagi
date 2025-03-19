package ai.openai.pojo;

public class PromptTokensDetails {
    private long cached_tokens;

    public long getCached_tokens() {
        return cached_tokens;
    }

    public void setCached_tokens(long cached_tokens) {
        this.cached_tokens = cached_tokens;
    }

    @Override
    public String toString() {
        return "PromptTokensDetails [cached_tokens=" + cached_tokens + "]";
    }
}
