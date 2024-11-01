package ai.llm.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SenseChatUsage {
    private Integer prompt_tokens;
    private Integer completion_tokens;
    private Integer knowledge_tokens;
    private Integer total_tokens;
}
