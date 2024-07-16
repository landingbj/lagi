package ai.medusa.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(exclude = {"maxTokens"})
public class PromptParameter {
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private String category;
}
