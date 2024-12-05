package ai.agent.chat.zhipu.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usage {
    private Integer prompt_tokens;
    private Integer completion_tokens;
    private Integer total_tokens;
}
