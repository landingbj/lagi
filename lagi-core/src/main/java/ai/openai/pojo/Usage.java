package ai.openai.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Usage  implements Serializable {
    private long prompt_tokens;
    private long completion_tokens;
    private long total_tokens;
    private PromptTokensDetails prompt_tokens_details;
    private CompletionTokensDetails completion_tokens_details;
    private long prompt_cache_hit_tokens;
    private long prompt_cache_miss_tokens;
}
