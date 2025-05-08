package ai.openai.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PromptTokensDetails implements Serializable {
    private long cached_tokens;
}
