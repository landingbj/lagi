package ai.medusa.pojo;

import ai.openai.pojo.ChatCompletionResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CacheItem {
    private PromptInput promptInput;
    private ChatCompletionResult chatCompletionResult;
}
