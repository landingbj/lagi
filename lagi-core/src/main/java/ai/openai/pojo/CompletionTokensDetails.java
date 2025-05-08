package ai.openai.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompletionTokensDetails implements Serializable {
    private long reasoning_tokens;
}
