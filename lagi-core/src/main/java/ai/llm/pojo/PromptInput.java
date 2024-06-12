package ai.llm.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class PromptInput {
    private List<String> promptList;
    private Double temperature;
    private Integer maxTokens;
    private String category;
}
