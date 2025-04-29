package ai.medusa.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class PromptParameter {
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private String category;
}
