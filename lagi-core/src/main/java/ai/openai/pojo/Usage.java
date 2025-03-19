package ai.openai.pojo;

public class Usage {
    private long prompt_tokens;
    private long completion_tokens;
    private long total_tokens;
    private PromptTokensDetails prompt_tokens_details;
    private CompletionTokensDetails completion_tokens_details;
    private long prompt_cache_hit_tokens;
    private long prompt_cache_miss_tokens;

    public long getPrompt_tokens() {
        return prompt_tokens;
    }

    public void setPrompt_tokens(long prompt_tokens) {
        this.prompt_tokens = prompt_tokens;
    }

    public long getCompletion_tokens() {
        return completion_tokens;
    }

    public void setCompletion_tokens(long completion_tokens) {
        this.completion_tokens = completion_tokens;
    }

    public long getTotal_tokens() {
        return total_tokens;
    }

    public void setTotal_tokens(long total_tokens) {
        this.total_tokens = total_tokens;
    }

    public PromptTokensDetails getPrompt_tokens_details() {
        return prompt_tokens_details;
    }

    public void setPrompt_tokens_details(PromptTokensDetails prompt_tokens_details) {
        this.prompt_tokens_details = prompt_tokens_details;
    }

    public CompletionTokensDetails getCompletion_tokens_details() {
        return completion_tokens_details;
    }

    public void setCompletion_tokens_details(CompletionTokensDetails completion_tokens_details) {
        this.completion_tokens_details = completion_tokens_details;
    }

    public long getPrompt_cache_hit_tokens() {
        return prompt_cache_hit_tokens;
    }

    public void setPrompt_cache_hit_tokens(long prompt_cache_hit_tokens) {
        this.prompt_cache_hit_tokens = prompt_cache_hit_tokens;
    }

    public long getPrompt_cache_miss_tokens() {
        return prompt_cache_miss_tokens;
    }

    public void setPrompt_cache_miss_tokens(long prompt_cache_miss_tokens) {
        this.prompt_cache_miss_tokens = prompt_cache_miss_tokens;
    }

    @Override
    public String toString() {
        return "Usage [prompt_tokens=" + prompt_tokens + ", completion_tokens=" + completion_tokens + ", total_tokens="
                + total_tokens + ", prompt_tokens_details=" + prompt_tokens_details + ", completion_tokens_details="
                + completion_tokens_details + ", prompt_cache_hit_tokens=" + prompt_cache_hit_tokens
                + ", prompt_cache_miss_tokens=" + prompt_cache_miss_tokens + "]";
    }
}
